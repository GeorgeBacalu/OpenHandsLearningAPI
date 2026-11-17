# Free pilot deployment

The user selected direct APK distribution outside Google Play and a $0 budget.

- API: https://openhandslearning-api.onrender.com
- Render service: https://dashboard.render.com/web/srv-dbu0di6k1f9s739v15b0
- Neon project: https://console.neon.tech/app/projects/late-smoke-70539515
- Database: PostgreSQL 18, project `openhandslearning`, production branch, Frankfurt.
- Public media bucket: `openhandslearning-media` in that same Neon project.
- Media origin: https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/prod/

Render uses the **Free** compute plan and Neon uses a separate **Free** organization
named Open Hands Learning. No paid service or payment card is needed for this
configuration. Free tiers have quotas and can suspend service when exhausted;
Render also sleeps while idle. This is a small pilot deployment, not an availability
guarantee. Existing AWS resources remain separate from this deployment and should
be reviewed separately if the whole AWS account must have zero cost.

## Runtime configuration

Render builds `Dockerfile`, activates `SPRING_PROFILES_ACTIVE=render`, and checks
`/openapi.json`. It receives `DATABASE_JDBC_URL`, `POSTGRES_USERNAME`, and
`POSTGRES_PASSWORD` as private environment variables. Never commit their values.
The JDBC URL uses the direct Neon endpoint with `sslmode=require`; Flyway runs at
startup. The application needs no AWS credentials to serve its catalog.

The build image installs `unzip` because the Maven wrapper's pinned checksum is
for the ZIP distribution. Without it, the Unix wrapper chooses a tarball and
correctly rejects the mismatched checksum. Checksum verification remains enabled.

## Initial content

The initial import copied only categories and the public school directory:
207 categories, 42 counties, 13,755 cities/localities, and 18,022 schools. It did
not copy users, password hashes, or favorites. The source export is in ignored
`build/distribution/catalog-20261117.csv`.

`infrastructure/prepare-free-media.py` recovers 428 media files (about 142 MB)
from the Android repository's local Git history, with SHA-256 verification. It
does not make S3 requests. `upload-free-media.cjs` uploads those files only to
the approved app bucket and verifies every object's length and hash metadata.

The maintenance scripts use the official `@neondatabase/serverless` and
`@aws-sdk/client-s3` packages installed under ignored `build/distribution/tools`.
Connection files under `build/distribution/*.private.json` are temporary local
secrets; remove them when the maintenance work is finished. They must never be
included in source commits, Docker contexts, APKs, or release attachments.

User registration remains the app's normal registration flow. Existing local
accounts were intentionally not migrated, so users create a new account on the
hosted backend.

## Validation

```powershell
.\mvnw.cmd -o package
.\mvnw.cmd -o '-Dtest=FreeMediaUrlTest,CategoryServiceImplTest,MediaIntegrationTest' test
```

Check `/openapi.json` and `/api/counties` after deploying, then exercise account
registration, login, category retrieval, image loading, and MP4 playback using
the signed Android release. Media URL migration must happen only after the
Neon objects are uploaded and V17 has been applied.

Provider limits checked on 2026-11-17:

- https://render.com/docs/free
- https://neon.com/pricing
- https://neon.com/docs/storage/overview
