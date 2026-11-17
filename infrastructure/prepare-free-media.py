"""Recover the app's own media from local Git history without requesting S3.

Output goes into ignored build/distribution. No account or user data is read.
"""
import csv
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import subprocess
from urllib.parse import urlparse

API = Path(__file__).resolve().parents[1]
UI = API.parent / "OpenHandsLearningUI"
OUTPUT = API / "build" / "distribution"
MEDIA = OUTPUT / "media"
SOURCE_REVISION = "012ab29"
CATALOG_REVISION = "d0b7c97"
JAVA_CATALOG = "app/src/main/java/com/project/ohl/presentation/MediaCatalog.java"


def git(*arguments):
    return subprocess.run(["git", "-C", str(UI), *arguments],
                          check=True, capture_output=True).stdout


def entries(source):
    return re.findall(r'Map\.entry\("([^"]+)",\s*"([^"]+)"\)', source)


def main():
    csv.field_size_limit(16 * 1024 * 1024)
    old_entries = entries(git("show", f"{CATALOG_REVISION}:{JAVA_CATALOG}").decode())
    old_paths = {path: name for name, path in old_entries}
    current_entries = entries((UI / JAVA_CATALOG).read_text(encoding="utf-8"))
    current_paths = {path: name for name, path in current_entries}
    available = set(git("ls-tree", "-r", "--name-only", SOURCE_REVISION,
                        "app/src/main/res").decode().splitlines())
    with (OUTPUT / "catalog-20261117.csv").open(encoding="utf-8", newline="") as stream:
        catalog = json.loads(next(csv.DictReader(stream))["payload"])
    requested = {}
    for category in catalog["categories"]:
        for field in ("image_url", "video_url"):
            if not category.get(field):
                continue
            remote_path = urlparse(category[field]).path.lstrip("/")
            key = re.sub(r"^(dev|prod)/", "", remote_path)
            if not key.startswith("categories/"):
                raise ValueError(f"Unexpected catalog path: {remote_path}")
            path = PurePosixPath(key)
            candidates = [key]
            if path.stem == "main":
                candidates += [str(path.parent) + path.suffix,
                               str(path.parent / (path.parent.name + path.suffix))]
            source_name = next((old_paths[p] for p in candidates if p in old_paths), None)
            if source_name is None:
                source_name = current_paths.get(key)
            if source_name is None:
                raise ValueError(f"No historical media mapping: {key}")
            requested[key] = (source_name, path.suffix)
    for name, key in current_entries:
        requested.setdefault(key, (name, PurePosixPath(key).suffix))

    manifest = []
    missing = []
    manifest_file = OUTPUT / "media-manifest.json"
    previous = {item["key"]: item for item in json.loads(manifest_file.read_text())} if manifest_file.exists() else {}
    for key, (name, extension) in sorted(requested.items()):
        name = {"overlay": "overlay_background"}.get(name, name)
        folder = "raw" if extension == ".mp4" else "drawable-nodpi"
        source = f"app/src/main/res/{folder}/{name}{extension}"
        if source not in available:
            missing.append({"key": key, "source": source})
            continue
        destination = MEDIA / key
        cached = previous.get(key)
        if cached and cached["source"] == source and destination.exists() and hashlib.sha256(destination.read_bytes()).hexdigest() == cached["sha256"]:
            manifest.append(cached)
            continue
        data = git("show", f"{SOURCE_REVISION}:{source}")
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_bytes(data)
        manifest.append({"key": key, "source": source, "bytes": len(data),
                         "sha256": hashlib.sha256(data).hexdigest()})
    (OUTPUT / "media-manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"files": len(manifest), "bytes": sum(x["bytes"] for x in manifest),
                      "missing": missing}, ensure_ascii=False))
    if missing:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
