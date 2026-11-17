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
`/openapi.json`. The `render` profile includes `prod` through a Spring profile
group; it only adds Render's internal port setting.

| Profile | Database | Media operations and public URLs |
| --- | --- | --- |
| `dev` (default) | Local `open-hands-learning` | AWS S3 `open-hands-learning/dev/` |
| `prod` | Neon via `DATABASE_JDBC_URL` | Neon `openhandslearning-media/prod/` |
| `render` | Includes `prod` | Includes `prod` |

The obsolete localhost production database configuration has been removed.
Production requires these private environment variables:

- `DATABASE_JDBC_URL`, `POSTGRES_USERNAME`, `POSTGRES_PASSWORD`
- `NEON_STORAGE_ACCESS_KEY_ID`, `NEON_STORAGE_SECRET_ACCESS_KEY`

The Neon storage credential must have `storage:write` scope (which includes
read). It is used only by the server; never package it in an APK or commit it.
Production cannot fall back to AWS credentials if the Neon credential is absent.
The JDBC URL uses the direct Neon endpoint with `sslmode=require`; Flyway runs at
startup. The deleted local `open-hands-learning-prod` database and AWS S3 `prod/`
prefix are not used.

Development continues to accept the existing `AWS_ACCESS_KEY` and
`AWS_SECRET_ACCESS` variables, or the normal AWS credential provider chain when
both are empty. Production uses an explicit HTTPS endpoint and path-style S3
requests; development retains AWS's normal endpoint resolution. Both use the
same streaming storage adapter and administrator-protected upload/delete routes.

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
.\mvnw.cmd -o '-Dtest=S3StorageConfigTest,MediaIntegrationTest,ProductionMediaIntegrationTest,MediaPathTest' test
```

The opt-in `MediaStorageSmokeTest` performs real upload/read/delete round trips
against both providers when `OHL_MEDIA_STORAGE_SMOKE=true` and both providers'
credentials are set. It creates only unique `categories/deployment_checks/`
objects and removes them in cleanup. Ordinary test runs never contact storage.

Check `/openapi.json` and `/api/counties` after deploying, then exercise account
registration, login, category retrieval, image loading, and MP4 playback using
the signed Android release. Media URL migration must happen only after the
Neon objects are uploaded and V17 has been applied.

Provider limits checked on 2026-11-17:

- https://render.com/docs/free
- https://neon.com/pricing
- https://neon.com/docs/storage/overview
- https://neon.com/docs/storage/authentication
