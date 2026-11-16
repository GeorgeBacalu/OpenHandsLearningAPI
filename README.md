# Open Hands Learning

REST API for the Open Hands Learning Android application.

## Build and test

Install JDK 25 and set `JAVA_HOME` to that installation. Java 25 is required to
build and run the API; JDK 17 is not required. The Maven Wrapper downloads Maven
3.10.0 and verifies its checksum. No global Maven installation or wrapper JAR is
needed.

Source compilation treats warnings as errors. `.mvn/jvm.config` explicitly
allows the Unsafe access required by Lombok 1.18.48 during Java 25 compilation,
using the compatibility option described in [JEP 498](https://openjdk.org/jeps/498).
This setting applies to Maven's JVM; it is not required to run the packaged API.

```powershell
.\mvnw.cmd clean verify
```

On macOS/Linux, use `./mvnw clean verify`. Database tests use an isolated H2
database. Hibernate creates the schema when the test context starts and drops it
when the context closes. Each database-backed test loads `src/test/resources/test.sql`
with two records per table. The shared fixture clears all rows
before and after each test, and verifies the row counts. The full catalog is not loaded.

Tests cover persistence, foreign keys, HTTP errors, BCrypt,
MapStruct mappings, OpenAPI coverage, and architecture boundaries. A separate
Flyway test verifies schema creation, empty application tables, and repeat runs.

## Run locally

The PostgreSQL database already contains the schema and data. Set
`POSTGRES_USERNAME` and `POSTGRES_PASSWORD`, then run:

```powershell
.\mvnw.cmd spring-boot:run
```

`src/main/resources/application.properties` contains the connection, schema
validation, and timezone settings. Hibernate validates the existing development
schema; startup does not create tables or seed data. No dev profile is required.

All entity IDs and foreign keys use UUIDs. New records receive random UUIDs;
Persisted entity IDs are UUID strings. The read-only section menu uses fixed type identifiers. Categories store public S3 image URLs in `image_url`; category and section responses use `imageUrl`. The logo remains local to Android.

## Database migrations

Flyway is explicitly disabled in `application.properties`; Hibernate only validates
schema on startup. Existing development databases may use the original numbering
through V16; this branch consolidates and renumbers that history as described below.
The immutable V1 captures
the previous UUID schema. V2 adds:

- Per-user favorites; existing shared favorites are copied to each existing user.
- Schools and normalized user membership.
- Unique normalized account emails, required values, and supported role values.
- Stable catalog `code`, `mediaKey`, and section `iconKey` fields, independent of translated display names.
- Query indexes, parent relationships, and a serialized database trigger preventing hierarchy cycles.

V2 transforms existing records and memberships; it seeds no users or catalog
content. Space characters remain valid sign entries. New empty databases contain
no application records after V1 through V7. Back up and rehearse upgrades on a copy
before deploying these branches. Existing duplicate account emails must be resolved explicitly; migration failures
roll back V2.
Migrate the database through V15 before running this backend. See the S3 migration notes below for the consolidated migration history.

V3 corrects Romanian media-key transliteration, removes the obsolete duplicate
`Ă` sign, and replaces resource integers with media keys throughout the catalog.
The current alphabet uses letter media keys such as `b`, with distinct demonstrations for
A and the Romanian diacritics. Android category photos and voice illustrations
have separate resource catalogs. These catalog data updates are maintained
directly in the database; historical applied migrations retain their checksums.
Letters are unique with case preserved,
and normalized category names are unique among siblings, including
root categories. Shared media keys remain allowed.

V4 removes the row revision counters from all nine entity tables. Applied
migration files remain unchanged so Flyway can validate existing installations.

V5 removes category display_order. V11 defines section ordering by UUID in application
code. Category lists, including favorites and filtered searches, use Java Stream sorting with Romanian alphabetical collation, ignoring case and using UUID
as a stable tie-breaker. /api/categories/filter returns only root categories
when no parent is supplied, or only the selected parent's direct children. Both
levels are alphabetically ordered. The obsolete category ordering index and
constraint are removed with the column; the sibling-name index supports parent
filtering after V12.

V6 removes category codes. Category navigation and direct-child filters use UUIDs;
the category filter accepts optional parentId and name; V12 removes sectionId.
Renaming a parent leaves its UUID, children, and media keys unchanged. V11 also
removes section codes; Android selects main section screens by UUID.

V7 removes the letter-sign table and its CRUD API. The fixed alphabet mappings
are an immutable Android dictionary shared by text and voice conversion.
No backend request is needed for letter-to-sign lookup. The previous mappings
are preserved in the local dictionary, including Romanian aliases and spaces.
The pre-migration database backup retains the retired table.

V8 replaces institutions with a Romania-only directory: `counties` →
`cities` → `schools`. Schools use `city_id`; users use `school_id`.
The cities table includes villages and Bucharest sectors, as represented by SIRUTA.
Existing account UUIDs remain unchanged.

Populate the directory explicitly after taking a database backup:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--spring.flyway.enabled=true --app.school-directory.import=true'
```

The importer reads the [INS SIRUTA S1 2026 catalog](https://data.gov.ro/dataset/siruta_s1-2026)
and [Ministry school network 2025–2026](https://data.gov.ro/dataset/retea-scolara-2025-2026)
through the data.gov.ro CKAN API. These are the verified source editions; imports
do not automatically switch to a new edition. Apache POI 6.0.0 reads XLSX and
Commons CSV 1.14.1 reads SIRUTA. No directory records are seeded by Flyway.
Normal startup makes no external directory requests.

Official SIRUTA and SIIIR codes determine repeatable UUIDs. Import validates both
files and all school locations before writing, then upserts the complete directory
in one transaction. Repeat imports preserve UUIDs and unchanged rows. Records absent
from a later source are retained so memberships are not silently deleted.
The three original numbered placeholder schools are replaced by the official
Glina, Cățelu–Glina and Bobești–Glina schools; their users transfer to the corresponding schools. A database check rejects schools without a city; it is validated once
legacy placeholders have been replaced. A failed import rolls back its changes.

Browse `/api/counties`,
`/api/cities?countyId=...`, and `/api/schools/search?cityId=...`.
City and school searches accept `name`, `offset` (default 0), and `limit`
(default 50, maximum 200). School CRUD is at `/api/schools`; requests use
`name` and `cityId`. User requests use `schoolId`.

V9 removes classroom management, its table, and user classroom assignments.
Users retain their school memberships and all other account data. The API no
longer exposes classroom endpoints or a classroom reference on users.

V10 removes feedback and notification management, both tables, and their API
contracts. Remaining account, school, category and favorite data are preserved.
The event-only clock configuration and timezone property are no longer needed.

V11 keeps user ID, name, email, phone, password, role and school only. It renames
`mobile` to `phone` without changing values, and removes address and birthday.
The profile API uses `phone`; responses continue to exclude password hashes.
Existing user UUIDs, credentials, roles and school memberships are preserved.

V11 renames `user_favorites` to `favorites` and drops `created_at`. Each row is
only a user/category UUID pair. Existing pairs, composite uniqueness and cascading
foreign keys are preserved; favorite lists continue to use alphabetical category order.

V11 removes section `display_order` and `code`. V13 replaces UUID-based section
ordering with the fixed declaration order of the API's Section enum.

The retired Text to Sign section was deleted directly as a catalog record change,
without a schema migration. It had no linked categories in development. Remaining
retained section IDs and all other records are preserved. Voice to Sign retains both typed
input and speech recognition unchanged.

V12 removes `categories.section_id` and replaces the composite parent foreign key
with a parent-only foreign key. Sibling-name uniqueness covers the entire catalog,
including roots; the database trigger still prevents hierarchy cycles. Categories
use UUID, name, parent UUID and media key only. `/api/categories/filter` returns
alphabetical roots when parentId is absent and direct children when it is supplied.

The six expression categories now sit under the `Expresii` parent inside Video.
The parent reuses the former Expressions section UUID; existing expression IDs,
media keys, descendant links and favorites are retained. The two remaining main
sections are Video and Voice to Sign. Category updates need no sectionId.

Category/subcategory creation has been removed from Android and the API. The
`/api/categories` collection supports GET only; POST returns 405 even for an
authenticated administrator. Existing catalog reads, updates, deletion and favorites
remain available. This change does not modify existing database records.

Catalog record maintenance is separate from V12. On an existing V11 database,
back up and regroup expressions before applying the schema migration:

```sql
BEGIN;
INSERT INTO categories (id, name, parent_id, section_id, media_key)
SELECT id, 'Expresii', NULL, '20a6880c-2e37-32e3-8e45-07a17e99a000', media_key
FROM sections WHERE id = '1deb5200-b06d-3977-9426-e2ea373195f9';
UPDATE categories
SET parent_id = coalesce(parent_id, '1deb5200-b06d-3977-9426-e2ea373195f9'),
    section_id = '20a6880c-2e37-32e3-8e45-07a17e99a000'
WHERE section_id = '1deb5200-b06d-3977-9426-e2ea373195f9';
DELETE FROM sections WHERE id = '1deb5200-b06d-3977-9426-e2ea373195f9';
COMMIT;
```

V13 drops the unreferenced `sections` table. `GET /api/sections` remains a read-only,
database-independent menu in fixed order: VIDEO, then VOICE_TO_SIGN. Each response
entry has type, name, imageUrl and iconKey; section UUIDs and section mutation/detail
endpoints are removed. Menu definitions and ordering can only change in API code.
All remaining application records are preserved. Applied migration files remain immutable.
Section display names are Romanian: „Secțiune video” and „Secțiune voce-semn”. Enum
types and media keys stay unchanged. Catalog spelling corrections are record
maintenance after a backup, rather than schema migrations.

For a separate empty PostgreSQL database:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/open-hands-learning-new"
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--spring.flyway.enabled=true'
```

For an audited copy already matching V1 with no Flyway history, explicitly
baseline it at version 1 before applying subsequent migrations. Automatic baselining remains disabled
in the application's default configuration. Never edit applied migrations.

Regular tests use Hibernate-created H2 schemas, two records per table. The
Flyway baseline test checks V1 creation and repeat runs on H2. V2 is PostgreSQL
specific because its hierarchy trigger serializes concurrent tree changes. Its
upgrade and record preservation were verified using a temporary database copy;
there is no PostgreSQL provisioning or rehearsal configuration in the project.

Favorites endpoints require `userId`; a category read accepts optional `userId`
to return that user's favorite status. Favorite requests require a valid JWT, and the supplied user UUID must match
the authenticated account. Account listing and CRUD, role management, and catalog
mutations require the ADMIN role. Catalog filters use the selected parent UUID. Clients can
rename category labels without changing UUIDs, children or media keys.

## HTTP contract

The checked-in OpenAPI 3.2.1 contract is
`src/main/resources/static/openapi.json`, served at
`http://localhost:8080/openapi.json`. Import it into an OpenAPI-compatible client
or documentation viewer. Contract tests verify that its operations match the
controllers. There is no runtime Swagger UI or springdoc dependency.

Errors use RFC 9457 problem details (`application/problem+json`), including field
validation errors. Database conflicts return 409 without exposing SQL details.
New and updated user passwords use Spring Security PBKDF2-HMAC-SHA256 with
600,000 iterations and a fresh 16-byte random salt. Existing BCrypt credentials
remain valid without rewriting accounts. Passwords are required, with no minimum
length, maximum length or composition policy. User responses exclude passwords.
Stored credentials
use `passwordHash` in Java and `users.password_hash` in PostgreSQL. Request-only
`password` fields accept raw passwords; response mappings never copy stored hashes.

V11 renames the password column to `password_hash` without changing credential
values. There is no password-hashing migration; existing accounts must already
store supported hashes. Registration and password changes hash passwords in the
application. Migrations and directory imports remain disabled during normal startup.

## Authentication

`POST /api/auth/register` creates an account with the USER role and a securely hashed password.
`POST /api/auth/login` verifies email and password. Both return
`{accessToken, tokenType: "Bearer", expiresIn: 1800, user}`. `user` contains the account
name and UUID, never its password. Send `Authorization: Bearer <accessToken>` on
subsequent protected requests. The API uses Spring Security's JWT resource-server
support and Nimbus signing/validation; no external OAuth2 provider is involved.

Tokens use HS256, a cryptographically random 256-bit private key, issuer, audience,
account UUID, unique token ID, issue time and a fixed 30-minute expiry. The API
checks signature, algorithm, issuer, audience, timestamps and revocation. It reads
the current account and role on every authenticated request. Expired tokens require
login again. `GET /api/auth/me` returns the current account; `POST /api/auth/logout`
revokes that token until expiry. Revocations and the signing key remain in memory:
restarting this single-instance API signs everyone out. Multiple API instances
would require shared signing keys and persistent revocation/throttling storage.

Sections, categories and favorites require authentication. User and role management
and catalog mutations require ADMIN; favorites must belong to the authenticated
account. Login, registration and the county/city/school-search reads needed by
registration remain public. Requests use no session cookies, and the API is
stateless with CSRF disabled because credentials are sent only in an explicit
Authorization header. Use HTTPS outside local development.

Existing BCrypt accounts keep their credentials and records; no database migration
is needed. Login is limited to five failures per email/IP in 15 minutes and
registration to 30 attempts per IP in 15 minutes. The Android app encrypts JWTs
with Android Keystore outside backups, validates them with `/api/auth/me`, and
redirects guests to login before showing the main page.

Tests cover guest restrictions, JWT issuance, invalid/tampered/expired tokens,
logout revocation, roles, favorite ownership, registration and throttling.

Reference: [Spring Security JWT support](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

## Structure

The alphabet catalog stores distinct media keys for Ă (`a_breve`),
Â (`a_circumflex`), Î (`i_circumflex`), Ș (`s_comma`)
and Ț (`t_comma`). A keeps `a`. These catalog data corrections
are already persisted in the database and do not require schema migrations.
Category IDs, names, hierarchy, favorites and customized media keys are preserved.
The Android repository documents the verified clip sources in `media/alphabet/README.md`.

- `domain`: framework-independent models and domain exceptions.
- `application`: input/output ports, use cases, validation models, and MapStruct mappings.
- `adapter/web`: HTTP controllers, response projections, and error handling.
- `adapter/persistence`: JPA entities, Spring Data repositories, and persistence adapters.
- `adapter/config`: dependency wiring, service transaction boundaries, password hashing.
- `adapter/directory`: opt-in government catalog downloads and parsing.

Application use cases depend on ports. Web and persistence adapters do not call
each other. Service transactions wrap complete use cases, and MapStruct-generated
DTO mapping uses scalar reference IDs without recursive entity loading. ArchUnit tests enforce these boundaries.

## Dependency baseline

The Java 25 baseline uses Spring Boot 4.1.2, Maven 3.10.0, and Maven Wrapper 3.3.4.
Spring Boot manages the compatible Spring Framework, Hibernate, Jackson 3,
PostgreSQL driver, Spring Security Crypto, and Lombok versions. Explicit overrides
keep Flyway, H2, JUnit, Mockito, and Maven plugins current. ArchUnit 1.5.1 enforces
architecture boundaries. MapStruct 1.6.3
provides compile-time DTO mapping; its processor and Lombok binding 0.2.0 are
configured explicitly for Java 25. Unmapped target fields fail compilation.
Generated mapper implementations stay in `target/`; stateless mappers are wired
as Spring beans and injected into application use cases. Versions and plugin
configuration live in `pom.xml`; wrapper settings and checksum live in
`.mvn/wrapper/maven-wrapper.properties`.

Release references: [Spring Boot](https://docs.spring.io/spring-boot/),
[Maven](https://maven.apache.org/download.cgi),
[Flyway](https://documentation.red-gate.com/fd/release-notes-for-flyway-engine-179732572.html),
[MapStruct](https://mapstruct.org/documentation/reference-guide/), and
[OpenAPI](https://spec.openapis.org/oas/v3.2.1.html).

IDE configuration, including `.idea` directories, and generated build output are
ignored and are not part of the repository.

## Account profile

`GET /api/auth/me` includes `schoolName`, `cityName`, and `countyName` alongside the account details.
`PUT /api/auth/password` accepts `currentPassword` and `newPassword`, verifies the current password, hashes the new password, and revokes the active token. Authenticate again after success.
`DELETE /api/auth/me` accepts `currentPassword` and permanently removes the authenticated account and its favorites. Other accounts are unaffected; tokens for the deleted account can no longer authenticate.
Both mutations require a bearer token, return 204 on success, reject an incorrect current password with 400, and throttle repeated attempts. User IDs in request bodies never select the target account.

## AWS S3 media

The backend uses AWS SDK for Java v2. The default bucket is `open-hands-learning`
and the AWS region code for US East (N. Virginia) is `us-east-1`.
For S3 writes, set `AWS_ACCESS_KEY` / `AWS_SECRET_ACCESS` or the standard
`AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` pair in the process running the API.
External Spring properties `app.storage.s3.access-key` and `app.storage.s3.secret-key`
take precedence. With neither key configured, the adapter uses the AWS default
credential chain, including shared profiles and workload IAM roles. Credentials
are resolved when S3 is accessed, so startup and catalog reads do not require
explicit S3 credentials. Supplying only one key fails startup.
Credentials are never included in responses.
Restart the terminal or IDE after changing Windows user environment variables.

The `app.storage.s3.bucket` and `app.storage.s3.region` Spring properties control
uploads and newly resolved URLs. Stored URLs are permanent absolute URLs: changing
the configuration does not rewrite database records or move objects between buckets.

Application artwork and clips use permanent public HTTPS URLs, without signing
or expiration. Logos remain bundled and unchanged. Set `MEDIA_ENVIRONMENT=dev`
(the default) or `prod`; application uploads are confined to that environment.

```
open-hands-learning/
  dev/
    voice/voice_a.webp
    categories/alfabet/alfabet.webp
    categories/alfabet/a.webp
    categories/alfabet/a.mp4
    categories/animale/animale_domestice/caine.webp
    categories/animale/animale_domestice/caine.mp4
    categories/obiecte/magazin/produse/ardei.webp
    categories/obiecte/magazin/produse/ardei.mp4
    categories/application/video_section.webp
  prod/
    voice/...
    categories/...
```

Folders follow the Romanian category names, normalized to lowercase ASCII and
underscores, with every ancestor retained. A category containing children has its
own folder; leaf files sit inside their parent's folder. Existing media filenames
remain stable, including separate A/Ă/Â/Î/Ș/Ț assets. `categories/application/`
holds noncatalog illustrations, icons and the splash clip. Voice illustrations and
voice-section artwork are under `voice/`.

Merge `infrastructure/s3-public-read-policy.json` into the bucket policy to permit
anonymous `s3:GetObject` only on the four application prefixes. Public policy
reads require `BlockPublicPolicy=false` and `RestrictPublicBuckets=false` at
applicable bucket/account levels. Keep ACL blocking enabled and Object Ownership
at Bucket owner enforced. The application never sends ACLs. The scoped uploader
identity policy is `infrastructure/s3-media-uploader-policy.json`; routine API use
needs object access only, while migration auditing also lists the bucket to check its structure.

The media API accepts `type=images|videos` and the query parameter `mediaKey`, a
relative object path such as `categories/alfabet/a.webp` or `voice/voice_a.webp`.
The configured environment is prepended. Paths reject traversal, other extensions,
wrong MIME types and cross-environment writes.

| Operation | Access | Result |
| --- | --- | --- |
| `GET /api/media/{type}?mediaKey=...` | Public | JSON `key` and permanent `url`; no existence check |
| `PUT /api/media/{type}?mediaKey=...` | Administrator bearer token | Upload/replace multipart part `file`; 204 |
| `DELETE /api/media/{type}?mediaKey=...` | Administrator bearer token | Idempotent delete; 204 |

Images must already be WebP (`image/webp`), and videos MP4 (`video/mp4`). The API
does not recompress uploads. The default multipart size limit is 100 MB; oversized
files return 413 and storage failures return a generic 503 Problem Detail.
Category responses include `imageUrl` and nullable `videoUrl`. Omitted URLs retain
their current values on category updates. Uploads do not automatically associate
media with categories.

### Compressing and migrating the existing catalog

`infrastructure/migrate_media.py` prepares and audits the media; its public
manifest records object paths, source files, byte sizes and SHA-256 hashes.
Install the pinned `infrastructure/media-requirements.txt` in a local environment.
Keep generated output, credentials and database backups outside Git.

1. Take a full development `pg_dump` backup and export categories to JSON
   (`SELECT json_agg(c) FROM (SELECT * FROM categories ORDER BY id) c`).
2. Run `prepare` with `--android`, `--snapshot`, `--output`, and `--manifest` paths.
   WebP uses maximum encoder effort (`method=6`): PNG artwork is lossless
   with exact RGBA pixel verification; JPEG photos start at quality 90, raising
   quality automatically until full-resolution RGB PSNR reaches 38 dB, with a
   lossless fallback if necessary. Dimensions and transparency are preserved. MP4 uses H.264
   `veryslow`, CRF 18, yuv420p, original audio and faststart. Already smaller
   clips retain their original video/audio streams with faststart remuxing.
   Dimensions and frame rate are retained; every output clip is fully decoded.
3. Run `audit` to preserve embedded ICC profiles, verify original video frame
   counts and durations, and require SSIM >= 0.98 against each original clip. Run `upload`
   for both `--environments dev prod`, then `verify`. Verification
   uses unsigned full GETs, correct Content-Type, byte count, complete SHA-256
   hashes, and 206 Range responses for every video.
4. Apply V15 with Flyway explicitly enabled. This additive migration preserves
   legacy image URLs during the transition, adds nullable `video_url`, and creates
   `media_assets` for all public asset URLs, including voice and section artwork.
5. Run `database` to verify public dev objects again, then update category URLs
   and the dev asset inventory in one transaction. The script checks that category
   UUIDs, names, hierarchy and favorites remain unchanged. Production DBs are
   not touched. `media_assets` exposes `object_key`, `public_url`, `content_type`,
   `byte_size`, and `sha256` for inspection in the development database.
6. Use the current S3-only Android build. It retains local logos and native UI
   vectors, and loads artwork and videos from S3 with the same layouts. Do not
   run `install` for this build: that legacy action restores bundled fallbacks
   for older Android revisions. Glide retains its normal downloaded-image cache.

Re-run `verify` after deployment. Legacy extensionless objects can be removed only
after all consumers have migrated and replacement URLs have passed verification.

### Consolidated migrations and image URLs

The old V11–V13 are combined in V11, which also renames `password` to
`password_hash` without changing values. The former V14/V15 are now V12/V13;
the standalone password-hashing migration is removed. New V14 renames `categories.media_key` to
`image_url`, widens it to 2048 characters, and replaces each existing key with
`https://open-hands-learning.s3.us-east-1.amazonaws.com/images/{old-key}`.
UUIDs, names, hierarchy, and favorites are preserved. V14 does not upload files:
the matching image objects must exist in S3. Historical section and letter-sign
tables were already removed; categories are the only remaining `media_key` column.

This intentionally changes the API from `mediaKey` to `imageUrl` for categories
and section artwork. Android consumers must read `imageUrl` to load remote images;
the Android companion branch reads those URLs and preserves the original
bundled resource identities as fallbacks. Keep logos local. V15 and the explicit
media maintenance above replace the transitional V14 URLs with hierarchical
`.webp`/`.mp4` URLs without editing the already applied V14 migration.

All migrations are SQL files in `src/main/resources/db/migration`.
For a fresh database, apply V1–V15 normally. For an existing installation that
already applied all original versions through V16, back up the database and
reconcile Flyway's history with `infrastructure/reconcile-migration-history.sql`
before applying the new V14 followed by V15. The script checks the expected old history and schema,
backs up the old history table, removes the absorbed and retired rows, and remaps the
remaining versions and checksums. Do not run Flyway repair blindly before this
step: the reused version numbers would otherwise identify different migrations.
Databases stopped partway through the former V11–V16 must finish that original
sequence before reconciliation. Hibernate validation and automatic Flyway migration
remain configured as before; no live database changes happen on normal startup.
