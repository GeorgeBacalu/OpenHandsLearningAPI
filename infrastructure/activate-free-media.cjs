// Switch only the imported app catalog after V17 and media verification succeed.
const fs = require('node:fs');
const path = require('node:path');
const { createRequire } = require('node:module');
const root = path.resolve(__dirname, '..');
const work = path.join(root, 'build', 'distribution');
const { neon } = createRequire(path.join(work, 'tools', 'package.json'))('@neondatabase/serverless');
const connection = JSON.parse(fs.readFileSync(path.join(work, 'neon-connection.private.json'), 'utf8'));
const mediaBase = 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/prod/';
const sourceOrigin = '^https://open-hands-learning[.]s3[.]us-east-1[.]amazonaws[.]com/(dev|prod)/';

async function main() {
    if (connection.database !== 'openhandslearning') throw new Error('Unexpected database.');
    const url = new URL('postgresql://' + connection.host + '/' + connection.database);
    url.username = connection.username; url.password = connection.password;
    url.searchParams.set('sslmode', 'require');
    const sql = neon(url.toString());
    const migrations = await sql`SELECT version FROM public.flyway_schema_history WHERE version = '17' AND success`;
    if (migrations.length !== 1) throw new Error('V17 has not been deployed yet.');
    const catalog = JSON.parse(fs.readFileSync(path.join(work, 'catalog.json'), 'utf8'));
    const manifest = new Set(JSON.parse(fs.readFileSync(path.join(work, 'media-manifest.json'), 'utf8')).map(item => item.key));
    for (const category of catalog.categories) {
        for (const field of ['image_url', 'video_url']) {
            if (!category[field]) continue;
            const key = 'prod/' + new URL(category[field]).pathname.replace(/^\/(dev|prod)\//, '');
            if (!manifest.has(key)) throw new Error('Missing uploaded media mapping: ' + key);
        }
    }
    const ids = catalog.categories.map(category => category.id);
    await sql.query(`UPDATE public.categories SET
        image_url = regexp_replace(image_url, $1, $2),
        video_url = regexp_replace(video_url, $1, $2)
        WHERE id = ANY($3::uuid[])`, [sourceOrigin, mediaBase, ids]);
    const [result] = await sql.query(`SELECT count(*)::int AS categories,
        count(*) FILTER (WHERE image_url LIKE $1 AND (video_url IS NULL OR video_url LIKE $1))::int AS using_free_media
        FROM public.categories WHERE id = ANY($2::uuid[])`, [mediaBase + '%', ids]);
    if (result.categories !== 207 || result.using_free_media !== 207) throw new Error('Catalog media verification failed.');
    console.log(JSON.stringify(result));
}
main().catch(error => {
    console.error(String(error.message).replaceAll(connection.password, '[redacted]'));
    process.exitCode = 1;
});
