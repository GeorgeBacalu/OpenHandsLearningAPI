// Server-side rearrangement of the existing app media. No file uploads.
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { createRequire } = require('node:module');
const work = path.resolve(__dirname, '..', 'build/distribution');
const sdk = createRequire(path.join(work, 'tools/package.json'))('@aws-sdk/client-s3');
const endpoint = 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech';
const bucket = 'openhandslearning-media';
const planFile = path.join(work, 'neon-root-move.json');
let settings;
async function main() {
    settings = JSON.parse(fs.readFileSync(path.join(work, 'neon-storage.private.json')));
    if (settings.AWS_ENDPOINT_URL_S3 !== endpoint) throw Error('Unexpected endpoint');
    const client = new sdk.S3Client({endpoint, region: settings.AWS_REGION, forcePathStyle:true, maxAttempts:4,
        credentials:{accessKeyId:settings.AWS_ACCESS_KEY_ID,secretAccessKey:settings.AWS_SECRET_ACCESS_KEY}});
    const head = key => client.send(new sdk.HeadObjectCommand({Bucket:bucket,Key:key}));
    async function list(prefix) {
        let token; const items=[];
        do {
            const page=await client.send(new sdk.ListObjectsV2Command({Bucket:bucket,Prefix:prefix,ContinuationToken:token}));
            items.push(...(page.Contents||[])); token=page.IsTruncated?page.NextContinuationToken:undefined;
        } while(token);
        return items;
    }
    async function each(items, action) {
        let next=0, done=0;
        await Promise.all(Array.from({length:4},async()=>{
            while(next<items.length) {
                await action(items[next++]);
                if(++done%25===0||done===items.length) console.log(`Verified ${done}/${items.length}`);
            }
        }));
    }
    try {
        if(process.argv[2]==='copy') {
            const manifest=JSON.parse(fs.readFileSync(path.join(work,'media-manifest.json')))
                .map(item=>({...item,key:item.key.replace(/^prod\//,'')}));
            if(manifest.length!==428||new Set(manifest.map(item=>item.key)).size!==428) throw Error('Unexpected manifest');
            const sources=await list('prod/');
            const allowed=new Set(manifest.map(item=>'prod/'+item.key));
            if(sources.filter(item=>allowed.has(item.Key)).length!==428||sources.some(item=>!allowed.has(item.Key)&&!(item.Key==='prod/'&&item.Size===0))) throw Error('Unexpected source keys');
            const plan={endpoint,bucket,verified:false,objects:[]};
            await each(manifest,async item=>{
                if(!/^(categories|voice)\/[a-z0-9_/]+\.(webp|mp4)$/.test(item.key)) throw Error('Unexpected key');
                const sourceKey='prod/'+item.key, source=await head(sourceKey);
                if(source.ContentLength!==item.bytes||source.Metadata?.sha256!==item.sha256) throw Error('Source mismatch: '+sourceKey);
                let target;
                try {target=await head(item.key)} catch(error) {if(error.$metadata?.httpStatusCode!==404) throw error;}
                if(target&&(target.ContentLength!==item.bytes||target.Metadata?.sha256!==item.sha256)) throw Error('Root collision: '+item.key);
                if(!target) {
                    await client.send(new sdk.CopyObjectCommand({Bucket:bucket,Key:item.key,CopySource:bucket+'/'+sourceKey.split('/').map(encodeURIComponent).join('/'),MetadataDirective:'COPY'}));
                    target=await head(item.key);
                }
                if(target.ContentLength!==item.bytes||target.Metadata?.sha256!==item.sha256||target.ContentType!==source.ContentType||target.CacheControl!==source.CacheControl) throw Error('Copy metadata mismatch: '+item.key);
                const response=await fetch(endpoint+'/'+bucket+'/'+item.key,{signal:AbortSignal.timeout(60000)});
                if(!response.ok) throw Error('Public read failed: '+item.key+': '+response.status);
                const hash=crypto.createHash('sha256'); let bytes=0;
                for await(const chunk of response.body) {hash.update(chunk);bytes+=chunk.length;}
                if(bytes!==item.bytes||hash.digest('hex')!==item.sha256) throw Error('Copy bytes mismatch: '+item.key);
                plan.objects.push({key:item.key,sourceKey,sourceETag:source.ETag,targetETag:target.ETag,bytes:item.bytes,sha256:item.sha256});
            });
            plan.verified=true; plan.objects.sort((a,b)=>a.key.localeCompare(b.key));
            fs.writeFileSync(planFile,JSON.stringify(plan,null,2));
            console.log('Verified 428 server-side root copies. Originals retained until deployment is verified.');
        } else if(process.argv[2]==='cleanup') {
            const plan=JSON.parse(fs.readFileSync(planFile));
            if(!plan.verified||plan.endpoint!==endpoint||plan.bucket!==bucket||plan.objects.length!==428) throw Error('No verified move plan');
            const sources=await list('prod/'), expected=new Set(plan.objects.map(item=>item.sourceKey));
            if(sources.some(item=>!expected.has(item.Key)&&!(item.Key==='prod/'&&item.Size===0))) throw Error('New source keys appeared');
            await each(plan.objects,async item=>{
                if(item.sourceKey!=='prod/'+item.key||!/^(categories|voice)\/[a-z0-9_/]+\.(webp|mp4)$/.test(item.key)) throw Error('Invalid plan key');
                const target=await head(item.key), source=sources.find(object=>object.Key===item.sourceKey);
                if(target.ETag!==item.targetETag||target.ContentLength!==item.bytes||target.Metadata?.sha256!==item.sha256) throw Error('Root copy changed: '+item.key);
                if(source&&source.ETag!==item.sourceETag) throw Error('Source changed: '+item.sourceKey);
            });
            await each(sources,item=>client.send(new sdk.DeleteObjectCommand({Bucket:bucket,Key:item.Key})));
            if((await list('prod/')).length) throw Error('Source prefix still contains objects');
            console.log('Verified prod/ is empty and all 428 root copies remain.');
        } else throw Error('Choose copy or cleanup');
    } finally {client.destroy();}
}
main().catch(error=>{
    let message=String(error.message);
    for(const name of ['AWS_ACCESS_KEY_ID','AWS_SECRET_ACCESS_KEY']) if(settings?.[name]) message=message.replaceAll(settings[name],'[redacted]');
    console.error(message);process.exitCode=1;
});
