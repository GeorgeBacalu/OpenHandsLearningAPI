// End-to-end hosted smoke check; creates and removes only its own synthetic user.
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const work = path.resolve(__dirname, '..', 'build', 'distribution');
const api = 'https://openhandslearning-api.onrender.com';
const media = 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/open-hands-learning/';

async function request(route, options = {}) {
    return fetch(api + route, { ...options, signal: AbortSignal.timeout(210000) });
}
async function main() {
    const source = JSON.parse(fs.readFileSync(path.join(work, 'catalog.json'), 'utf8'));
    const user = { name: 'Deployment Check', email: `deployment-check-${Date.now()}@example.invalid`,
        password: crypto.randomBytes(32).toString('base64url'), phone: '0700000000', schoolId: source.schools[0].id };
    let token;
    const headers = () => ({ 'Content-Type': 'application/json', Authorization: 'Bearer ' + token });
    try {
        const health = await request('/openapi.json');
        if (health.status !== 200) throw new Error('Health endpoint failed: ' + health.status);
        const counties = await (await request('/api/counties')).json();
        if (counties.length !== 42) throw new Error('County directory is incomplete.');
        const registration = await request('/api/auth/register', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(user) });
        if (registration.status !== 201) throw new Error('Registration failed: ' + registration.status);
        token = (await registration.json()).accessToken;
        const login = await request('/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: user.email, password: user.password }) });
        if (login.status !== 200) throw new Error('Login failed: ' + login.status);
        token = (await login.json()).accessToken;
        const profile = await request('/api/auth/me', { headers: headers() });
        if (profile.status !== 200) throw new Error('Authenticated profile failed.');
        const categoriesResponse = await request('/api/categories', { headers: headers() });
        if (categoriesResponse.status !== 200) throw new Error('Catalog request failed.');
        const categories = await categoriesResponse.json();
        if (categories.length !== 207 || categories.some(c => !c.imageUrl.startsWith(media) || (c.videoUrl && !c.videoUrl.startsWith(media)))) {
            throw new Error('Hosted catalog is not fully migrated to free media.');
        }
        const image = await fetch(categories[0].imageUrl);
        if (image.status !== 200 || !image.headers.get('content-type')?.includes('image/webp')) throw new Error('Public image failed.');
        await image.arrayBuffer();
        const videoCategory = categories.find(c => c.videoUrl);
        const video = await fetch(videoCategory.videoUrl, { headers: { Range: 'bytes=0-1023' } });
        if (video.status !== 206 || (await video.arrayBuffer()).byteLength !== 1024) throw new Error('Video byte-range playback failed.');
        console.log(JSON.stringify({ health: 200, counties: counties.length, registration: 201, login: 200,
            profile: 200, categories: categories.length, freeMediaOnly: true, image: 200, videoRange: 206 }));
    } finally {
        if (token) {
            const cleanup = await request('/api/auth/me', { method: 'DELETE', headers: headers(), body: JSON.stringify({ currentPassword: user.password }) });
            if (cleanup.status !== 204) throw new Error('Synthetic account cleanup failed: ' + cleanup.status);
            console.log('Removed synthetic smoke-test account.');
        }
    }
}
main().catch(error => { console.error(error.message); process.exitCode = 1; });
