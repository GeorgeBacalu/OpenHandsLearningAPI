// One-time import into this app's NEW, empty Neon database. Never imports users.
const fs = require('node:fs');
const path = require('node:path');
const { createRequire } = require('node:module');
const root = path.resolve(__dirname, '..');
const work = path.join(root, 'build', 'distribution');
const driverRequire = createRequire(path.join(work, 'tools', 'package.json'));
const { neon } = driverRequire('@neondatabase/serverless');
const connection = JSON.parse(fs.readFileSync(path.join(work, 'neon-connection.private.json'), 'utf8'));

async function main() {
    if (connection.database !== 'openhandslearning' ||
        !connection.host.endsWith('.neon.tech')) throw new Error('Unexpected database target.');
    const uri = new URL('postgresql://' + connection.host + '/' + connection.database);
    uri.username = connection.username;
    uri.password = connection.password;
    uri.searchParams.set('sslmode', 'require');
    const sql = neon(uri.toString());
    const tables = ['counties', 'cities', 'schools', 'categories'];
    const catalog = JSON.parse(fs.readFileSync(path.join(work, 'catalog.json'), 'utf8'));
    const countsQuery = 'SELECT ' + tables.map(name =>
        `(SELECT count(*)::int FROM public.${name}) AS ${name}`).join(', ');
    const [before] = await sql.query(countsQuery);
    if (Object.values(before).some(count => count !== 0)) {
        throw new Error('The target already has catalog data. Refusing to overwrite it.');
    }
    for (const table of tables) {
        if (!Array.isArray(catalog[table]) || catalog[table].length === 0) {
            throw new Error('Missing source data for ' + table);
        }
    }
    await sql.transaction(tables.map(table => sql.query(
        `INSERT INTO public.${table} SELECT * FROM jsonb_populate_recordset(NULL::public.${table}, $1::jsonb)`,
        [JSON.stringify(catalog[table])]
    )), { isolationLevel: 'Serializable' });
    const [after] = await sql.query(countsQuery);
    for (const table of tables) {
        if (after[table] !== catalog[table].length) throw new Error('Import count mismatch: ' + table);
    }
    console.log(JSON.stringify({ imported: after, usersImported: 0 }));
}

main().catch(error => {
    const message = String(error.message).replaceAll(connection.password, '[redacted]')
        .replaceAll(encodeURIComponent(connection.password), '[redacted]');
    console.error('Catalog import failed: ' + message);
    process.exitCode = 1;
});
