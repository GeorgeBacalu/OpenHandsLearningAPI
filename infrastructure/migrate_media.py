"""Prepare, upload and verify the existing Android catalog without changing its content.

Dependencies: Pillow, boto3, psycopg[binary], imageio-ffmpeg.
Run with --help. Credentials come only from environment variables/default AWS chain.
Generated files and database backups belong outside version control.
"""
import argparse
from concurrent.futures import ThreadPoolExecutor, as_completed
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import time
import unicodedata
import urllib.error
import urllib.request

BUCKET = "open-hands-learning"
REGION = "us-east-1"
BASE = f"https://{BUCKET}.s3.{REGION}.amazonaws.com/"


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def slug(value):
    value = unicodedata.normalize("NFD", value.lower())
    value = "".join(c for c in value if not unicodedata.combining(c))
    return re.sub(r"[^a-z0-9]+", "_", value).strip("_")


def preserve_profile(target, original):
    """Mux original ICC metadata without re-encoding the compressed image payload.

    RIFF/VP8X layout: https://developers.google.com/speed/webp/docs/riff_container
    """
    from PIL import Image
    profile = original.info.get("icc_profile")
    if not profile:
        return
    data = target.read_bytes()
    assert data[:4] == b"RIFF" and data[8:12] == b"WEBP"
    chunks, offset = [], 12
    while offset < len(data):
        kind = data[offset:offset + 4]
        size = int.from_bytes(data[offset + 4:offset + 8], "little")
        payload = data[offset + 8:offset + 8 + size]
        assert len(payload) == size
        chunks.append((kind, payload))
        offset += 8 + size + (size % 2)
    assert offset == len(data)
    extended = next((payload for kind, payload in chunks if kind == b"VP8X"), None)
    if extended is None:
        with Image.open(target) as image:
            flags = 0x10 if "A" in image.getbands() else 0
        extended = bytes([flags]) + b"\0" * 3 + (original.width - 1).to_bytes(3, "little") + (original.height - 1).to_bytes(3, "little")
    extended = bytes([extended[0] | 0x20]) + extended[1:]

    def chunk(kind, payload):
        return kind + len(payload).to_bytes(4, "little") + payload + (b"\0" if len(payload) % 2 else b"")

    body = b"WEBP" + chunk(b"VP8X", extended) + chunk(b"ICCP", profile)
    body += b"".join(chunk(kind, payload) for kind, payload in chunks if kind not in (b"VP8X", b"ICCP"))
    target.write_bytes(b"RIFF" + len(body).to_bytes(4, "little") + body)
    with Image.open(target) as image:
        assert image.info.get("icc_profile") == profile


def prepare(args):
    from PIL import Image
    import numpy as np
    import imageio_ffmpeg

    catalog = json.loads(args.snapshot.read_text(encoding="utf-8-sig"))
    by_id = {row["id"]: row for row in catalog}
    parents = {row["parent_id"] for row in catalog}
    resources = args.android / "app/src/main/res"
    assets, categories, assigned = [], [], set()

    def directory(row):
        chain, seen = [], set()
        current = row if row["id"] in parents or row["parent_id"] is None else by_id[row["parent_id"]]
        while current:
            if current["id"] in seen:
                raise ValueError("Category cycle")
            seen.add(current["id"])
            chain.insert(0, slug(current["name"]))
            current = by_id.get(current["parent_id"])
        return "categories/" + "/".join(chain)

    def add(source, key, content_type):
        assigned.add(source)
        assets.append({"source": str(source.relative_to(args.android)).replace("\\", "/"),
                       "key": key, "content_type": content_type,
                       "original_bytes": source.stat().st_size})

    for row in catalog:
        stem = row["image_url"].rsplit("/", 1)[-1].removesuffix(".webp")
        matches = list((resources / "drawable-nodpi").glob(stem + ".*"))
        if len(matches) != 1:
            raise ValueError(f"Expected one image for {stem}, got {matches}")
        folder = directory(row)
        image_key = f"{folder}/{stem}.webp"
        add(matches[0], image_key, "image/webp")
        clips = [p for p in (resources / "raw").glob(stem + ".*") if p.suffix in (".mp4", ".m4v")]
        if len(clips) > 1:
            raise ValueError(f"Duplicate video source for {stem}")
        video = clips[0] if clips else resources / "raw" / (stem + ".mp4")
        video_key = f"{folder}/{stem}.mp4" if video.exists() else None
        if video_key:
            add(video, video_key, "video/mp4")
        categories.append({"id": row["id"], "name": row["name"], "parent_id": row["parent_id"],
                           "image_key": image_key, "video_key": video_key})
    for source in sorted((resources / "drawable-nodpi").iterdir()):
        if source in assigned or "logo" in source.stem or source.stem == "openhandslearning_mark":
            continue
        key = ("voice/" if source.stem.startswith("voice_") or source.stem == "space"
               else "categories/application/") + source.stem + ".webp"
        add(source, key, "image/webp")
    for source in sorted((resources / "raw").iterdir()):
        if source not in assigned and source.suffix in (".mp4", ".m4v"):
            add(source, "categories/application/" + source.stem + ".mp4", "video/mp4")
    if len({a["key"] for a in assets}) != len(assets):
        raise ValueError("Duplicate destination keys")

    def compress(asset):
        source = args.android / asset["source"]
        target = args.output / asset["key"]
        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.exists() or target.stat().st_size == 0:
            if asset["content_type"] == "image/webp":
                # Maximum WebP encoder effort. PNG artwork retains every pixel and alpha;
                # JPEG photos use high quality to avoid lossless inflation of JPEG artifacts.
                with Image.open(source) as image:
                    lossless = source.suffix.lower() == ".png"
                    image.save(target, "WEBP", lossless=lossless,
                               quality=100 if lossless else 90, method=6, exact=True)
            else:
                # Keep every frame, original resolution, frame rate and audio. Very slow encoder
                # effort minimizes H.264 size at visually transparent quality, supported on API 24.
                command = [imageio_ffmpeg.get_ffmpeg_exe(), "-hide_banner", "-loglevel", "error", "-y",
                           "-i", str(source), "-map", "0:v:0", "-map", "0:a?", "-map_metadata", "-1",
                           "-c:v", "libx264", "-preset", "veryslow", "-crf", "18", "-pix_fmt", "yuv420p",
                           "-threads", "2", "-c:a", "copy", "-movflags", "+faststart", str(target)]
                subprocess.run(command, check=True)
                if target.stat().st_size >= source.stat().st_size:
                    # Avoid increasing already well compressed clips; still make them streamable.
                    subprocess.run([imageio_ffmpeg.get_ffmpeg_exe(), "-hide_banner", "-loglevel", "error", "-y",
                                    "-i", str(source), "-map", "0", "-c", "copy", "-map_metadata", "-1",
                                    "-movflags", "+faststart", str(target)], check=True)
        if asset["content_type"] == "image/webp":
            with Image.open(source) as original, Image.open(target) as compressed:
                assert original.size == compressed.size, source
                if source.suffix.lower() == ".png":
                    if original.convert("RGBA").tobytes() != compressed.convert("RGBA").tobytes():
                        raise ValueError(f"Pixels changed: {source}")
                    asset["quality_check"] = "identical RGBA pixels"
                else:
                    # Measure full-resolution RGB error. Raise quality automatically for
                    # difficult photos; never resize, crop or modify their composition.
                    reference = np.asarray(original.convert("RGB"), dtype=np.float32)
                    quality = 90
                    while True:
                        with Image.open(target) as candidate:
                            mse = np.mean((reference - np.asarray(candidate.convert("RGB"), dtype=np.float32)) ** 2)
                        psnr = float(10 * np.log10(255 ** 2 / mse)) if mse else 100.0
                        if psnr >= 38 or quality == 100:
                            break
                        quality = min(100, quality + 2)
                        original.save(target, "WEBP", quality=quality, method=6, exact=True)
                    if psnr < 38:
                        original.save(target, "WEBP", lossless=True, quality=100, method=6, exact=True)
                        psnr = 100.0
                    asset["quality_check"] = {"psnr_db": round(psnr, 2), "quality": quality}
            with Image.open(source) as original:
                preserve_profile(target, original)
        else:
            subprocess.run([imageio_ffmpeg.get_ffmpeg_exe(), "-hide_banner", "-loglevel", "error",
                            "-xerror", "-i", str(target), "-f", "null", "-"], check=True,
                           stdout=subprocess.DEVNULL, stderr=subprocess.PIPE)
        asset.update(bytes=target.stat().st_size, sha256=digest(target))
        return asset

    with ThreadPoolExecutor(max_workers=args.workers) as pool:
        for index, _ in enumerate(pool.map(compress, assets), 1):
            if index % 20 == 0:
                print(f"Compressed and verified {index}/{len(assets)}", flush=True)
    manifest = {"bucket": BUCKET, "region": REGION,
                "compression": {"images": "WebP method=6; PNG lossless exact alpha; JPEG quality>=90, PSNR>=38dB",
                                "videos": "H.264 CRF 18, veryslow, yuv420p, original audio, faststart; retain smaller originals"},
                "categories": categories, "assets": assets}
    args.manifest.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"assets": len(assets), "categories": len(categories),
                      "original_bytes": sum(a["original_bytes"] for a in assets),
                      "compressed_bytes": sum(a["bytes"] for a in assets)}), flush=True)


def client():
    import boto3
    from botocore.config import Config
    return boto3.client("s3", region_name=REGION,
                        endpoint_url=f"https://s3.{REGION}.amazonaws.com",
                        config=Config(connect_timeout=10, read_timeout=45, max_pool_connections=16,
                                      retries={"mode": "standard", "max_attempts": 10},
                                      s3={"addressing_style": "virtual"}),
                        aws_access_key_id=os.environ.get("AWS_ACCESS_KEY") or os.environ.get("AWS_ACCESS_KEY_ID"),
                        aws_secret_access_key=os.environ.get("AWS_SECRET_ACCESS") or os.environ.get("AWS_SECRET_ACCESS_KEY"))


def audit(args, manifest):
    import imageio_ffmpeg
    from PIL import Image

    for asset in manifest["assets"]:
        if asset["content_type"] == "image/webp":
            target = args.output / asset["key"]
            with Image.open(args.android / asset["source"]) as original:
                with Image.open(target) as previous:
                    pixels = previous.convert("RGBA").tobytes() if original.info.get("icc_profile") else None
                preserve_profile(target, original)
                if pixels is not None:
                    with Image.open(target) as result:
                        assert result.convert("RGBA").tobytes() == pixels
                    asset["color_profile"] = "original ICC bytes preserved"
            asset.update(bytes=target.stat().st_size, sha256=digest(target))

    videos = [asset for asset in manifest["assets"] if asset["content_type"] == "video/mp4"]

    def check(asset):
        original_frames, original_seconds = imageio_ffmpeg.count_frames_and_secs(str(args.android / asset["source"]))
        frames, seconds = imageio_ffmpeg.count_frames_and_secs(str(args.output / asset["key"]))
        if frames != original_frames or abs(seconds - original_seconds) > 0.1:
            raise ValueError(f"Video timing or frames changed: {asset['source']}: "
                             f"{original_frames}/{original_seconds} -> {frames}/{seconds}")
        asset["frame_check"] = {"frames": frames, "duration_seconds": seconds}
        comparison = subprocess.run([imageio_ffmpeg.get_ffmpeg_exe(), "-hide_banner", "-threads", "1",
                                     "-i", str(args.android / asset["source"]), "-threads", "1",
                                     "-i", str(args.output / asset["key"]), "-filter_complex_threads", "1",
                                     "-lavfi", "[0:v:0][1:v:0]ssim", "-an", "-f", "null", "-"],
                                    check=True, stdout=subprocess.DEVNULL, stderr=subprocess.PIPE, text=True)
        scores = re.findall(r"All:([0-9.]+)", comparison.stderr)
        if not scores or float(scores[-1]) < 0.98:
            raise ValueError(f"Video quality audit failed for {asset['source']}: SSIM {scores}")
        asset["frame_check"]["ssim"] = float(scores[-1])

    with ThreadPoolExecutor(max_workers=args.workers) as pool:
        list(pool.map(check, videos))
    args.manifest.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    print(f"Verified frame count, duration and SSIM >= 0.98 for all {len(videos)} MP4 clips", flush=True)


def upload(args, manifest):
    s3 = client()

    def put(item):
        from botocore.exceptions import ClientError
        environment, asset = item
        target = args.output / asset["key"]
        if digest(target) != asset["sha256"]:
            raise ValueError(f"Changed local output: {target}")
        key = environment + "/" + asset["key"]
        try:
            existing = s3.head_object(Bucket=BUCKET, Key=key)
            if (existing["ContentLength"] == asset["bytes"]
                    and existing.get("Metadata", {}).get("sha256") == asset["sha256"]
                    and existing["ContentType"] == asset["content_type"]):
                return
        except ClientError as error:
            if error.response["ResponseMetadata"]["HTTPStatusCode"] != 404:
                raise
        s3.upload_file(str(target), BUCKET, key,
                       ExtraArgs={"ContentType": asset["content_type"], "CacheControl": "public,max-age=86400",
                                  "Metadata": {"sha256": asset["sha256"]}})
    items = [(environment, asset) for environment in args.environments for asset in manifest["assets"]]
    with ThreadPoolExecutor(max_workers=args.workers) as pool:
        futures = [pool.submit(put, item) for item in items]
        try:
            for index, future in enumerate(as_completed(futures), 1):
                future.result()
                if index % 50 == 0 or index == len(items):
                    print(f"Uploaded or confirmed {index}/{len(items)}", flush=True)
        except Exception:
            for future in futures:
                future.cancel()
            raise


def verify(args, manifest):
    def public_get(request, count=5):
        for attempt in range(count):
            try:
                with urllib.request.urlopen(request, timeout=60) as response:
                    return response.status, response.headers, response.read()
            except urllib.error.HTTPError as error:
                if error.code not in (429, 500, 502, 503, 504) or attempt + 1 == count:
                    raise
                time.sleep(min(2 ** attempt, 8))
            except (OSError, TimeoutError):
                if attempt + 1 == count:
                    raise
                time.sleep(min(2 ** attempt, 8))

    def check(item):
        environment, asset = item
        url = BASE + environment + "/" + asset["key"]
        # Unsigned requests prove these are permanent public URLs, not SDK credentials.
        status, headers, data = public_get(url)
        assert status == 200, url
        assert headers.get_content_type() == asset["content_type"], url
        assert len(data) == asset["bytes"], url
        assert hashlib.sha256(data).hexdigest() == asset["sha256"], url
        if asset["content_type"] == "video/mp4":
            request = urllib.request.Request(url, headers={"Range": "bytes=0-1023"})
            status, _, chunk = public_get(request)
            assert status == 206, url
            assert chunk == data[:1024], url
        return url
    items = [(environment, asset) for environment in args.environments for asset in manifest["assets"]]
    with ThreadPoolExecutor(max_workers=args.workers) as pool:
        futures = [pool.submit(check, item) for item in items]
        urls = []
        try:
            for future in as_completed(futures):
                urls.append(future.result())
                if len(urls) % 100 == 0:
                    print(f"Verified public objects {len(urls)}/{len(items)}", flush=True)
        except Exception:
            for future in futures:
                future.cancel()
            raise
    print(f"Verified {len(urls)} public URLs, MIME types, complete SHA-256 hashes and all video Range requests", flush=True)
    return urls


def database(args, manifest):
    import psycopg
    from psycopg.rows import dict_row
    # A full pg_dump must be taken by the caller before applying the schema migration.
    # Public verification is a prerequisite to changing any catalog URL.
    args.environments = ["dev"]
    verify(args, manifest)
    with psycopg.connect(host="localhost", dbname="open-hands-learning",
                          user=os.environ["POSTGRES_USERNAME"], password=os.environ["POSTGRES_PASSWORD"],
                          row_factory=dict_row) as connection:
        with connection.cursor() as cursor:
            cursor.execute("LOCK TABLE categories IN EXCLUSIVE MODE")
            cursor.execute("SELECT id::text, name, parent_id::text FROM categories ORDER BY id")
            before = cursor.fetchall()
            expected = sorted(({k: c[k] for k in ("id", "name", "parent_id")} for c in manifest["categories"]), key=lambda c: c["id"])
            if before != expected:
                raise ValueError("Catalog identities or hierarchy changed after snapshot; re-prepare")
            cursor.execute("SELECT * FROM favorites ORDER BY user_id, category_id")
            favorites = cursor.fetchall()
            for category in manifest["categories"]:
                cursor.execute("UPDATE categories SET image_url=%s, video_url=%s WHERE id=%s",
                               (BASE + "dev/" + category["image_key"],
                                BASE + "dev/" + category["video_key"] if category["video_key"] else None, category["id"]))
            for asset in manifest["assets"]:
                key = "dev/" + asset["key"]
                cursor.execute("""INSERT INTO media_assets (object_key, public_url, content_type, byte_size, sha256)
                                  VALUES (%s,%s,%s,%s,%s) ON CONFLICT (object_key) DO UPDATE
                                  SET public_url=excluded.public_url, content_type=excluded.content_type,
                                      byte_size=excluded.byte_size, sha256=excluded.sha256""",
                               (key, BASE + key, asset["content_type"], asset["bytes"], asset["sha256"]))
            cursor.execute("SELECT * FROM favorites ORDER BY user_id, category_id")
            assert cursor.fetchall() == favorites
            cursor.execute("SELECT id::text, name, parent_id::text FROM categories ORDER BY id")
            assert cursor.fetchall() == before
    print("Updated development URLs and asset inventory; category identity, hierarchy and favorites preserved", flush=True)
    verify_database(args, manifest)


def verify_database(args, manifest):
    import psycopg
    with psycopg.connect(host="localhost", dbname="open-hands-learning",
                          user=os.environ["POSTGRES_USERNAME"], password=os.environ["POSTGRES_PASSWORD"]) as connection:
        with connection.cursor() as cursor:
            cursor.execute("SELECT id::text, image_url, video_url FROM categories")
            categories = {row[0]: row[1:] for row in cursor.fetchall()}
            assert len(categories) == len(manifest["categories"])
            for category in manifest["categories"]:
                assert categories[category["id"]] == (
                    BASE + "dev/" + category["image_key"],
                    BASE + "dev/" + category["video_key"] if category["video_key"] else None)
            cursor.execute("SELECT object_key, public_url, content_type, byte_size, sha256 FROM media_assets WHERE object_key LIKE 'dev/%'")
            assets = {row[0]: row[1:] for row in cursor.fetchall()}
            assert len(assets) == len(manifest["assets"])
            for asset in manifest["assets"]:
                key = "dev/" + asset["key"]
                assert assets[key] == (BASE + key, asset["content_type"], asset["bytes"], asset["sha256"])
    args.environments = ["dev"]
    verify(args, manifest)
    print(f"Read back and publicly verified {len(categories)} category rows and {len(assets)} development asset URLs", flush=True)


def install(args, manifest):
    for asset in manifest["assets"]:
        original = args.android / asset["source"]
        destination = original.with_suffix(".webp") if asset["content_type"] == "image/webp" else original.with_suffix(".mp4")
        shutil.copy2(args.output / asset["key"], destination)
        assert digest(destination) == asset["sha256"], destination
        if destination != original:
            original.unlink(missing_ok=True)
    print("Installed optimized WebP/MP4 bundled fallbacks; logos untouched")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=["prepare", "audit", "upload", "verify", "database", "verify_database", "install"])
    parser.add_argument("--android", type=Path, required=True)
    parser.add_argument("--snapshot", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--environments", nargs="+", choices=["dev", "prod"], default=["dev", "prod"])
    parser.add_argument("--workers", type=int, default=4)
    args = parser.parse_args()
    if args.action == "prepare":
        prepare(args)
    else:
        manifest = json.loads(args.manifest.read_text(encoding="utf-8"))
        globals()[args.action](args, manifest)


if __name__ == "__main__":
    main()
