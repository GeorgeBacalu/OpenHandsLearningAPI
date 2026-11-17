// Upload only media recovered from this app's Git history to its approved bucket.
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { createRequire } = require('node:module');
const work = path.resolve(__dirname, '..', 'build', 'distribution');
const sdkRequire = createRequire(path.join(work, 'tools', 'package.json'));
const { S3Client, PutObjectCommand, HeadObjectCommand } = sdkRequire('@aws-sdk/client-s3');
const settings = JSON.parse(fs.readFileSync(path.join(work, 'neon-storage.private.json'), 'utf8'));
const bucket = 'openhandslearning-media';
const expectedEndpoint = 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech';

async function main() {
    if (settings.AWS_ENDPOINT_URL_S3 !== expectedEndpoint) throw new Error('Unexpected media upload target.');
    const manifest = JSON.parse(fs.readFileSync(path.join(work, 'media-manifest.json'), 'utf8'));
    if (manifest.length !== 428) throw new Error('Unexpected media manifest; review before upload.');
    const client = new S3Client({ endpoint: expectedEndpoint, region: settings.AWS_REGION,
        credentials: { accessKeyId: settings.AWS_ACCESS_KEY_ID, secretAccessKey: settings.AWS_SECRET_ACCESS_KEY },
        forcePathStyle: true, maxAttempts: 5 });
    let next = 0;
    let completed = 0;
    try {
        await Promise.all(Array.from({ length: 4 }, async () => {
            while (next < manifest.length) {
                const item = manifest[next++];
                if (!/^prod\/(categories|voice)\/[a-z0-9_/]+\.(webp|mp4)$/.test(item.key)) throw new Error('Unexpected media path.');
                const body = fs.readFileSync(path.join(work, 'media', item.key));
                if (body.length !== item.bytes || crypto.createHash('sha256').update(body).digest('hex') !== item.sha256) {
                    throw new Error('Local media integrity check failed: ' + item.key);
                }
                await client.send(new PutObjectCommand({ Bucket: bucket, Key: item.key, Body: body,
                    ContentType: item.key.endsWith('.mp4') ? 'video/mp4' : 'image/webp',
                    CacheControl: 'public, max-age=86400', Metadata: { sha256: item.sha256 } }));
                const remote = await client.send(new HeadObjectCommand({ Bucket: bucket, Key: item.key }));
                if (remote.ContentLength !== item.bytes || remote.Metadata?.sha256 !== item.sha256) {
                    throw new Error('Uploaded media verification failed: ' + item.key);
                }
                if (++completed % 25 === 0 || completed === manifest.length) console.log(`Verified ${completed}/${manifest.length} media objects`);
            }
        }));
        console.log(JSON.stringify({ bucket, objects: completed, bytes: manifest.reduce((sum, item) => sum + item.bytes, 0) }));
    } finally { client.destroy(); }
}

main().catch(error => {
    let message = String(error.message);
    for (const key of ['AWS_ACCESS_KEY_ID', 'AWS_SECRET_ACCESS_KEY']) {
        if (settings[key]) message = message.replaceAll(settings[key], '[redacted]');
    }
    console.error('Media upload failed: ' + message);
    process.exitCode = 1;
});
