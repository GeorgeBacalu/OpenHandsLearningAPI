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
Persisted entity IDs are UUID strings. The read-only section menu uses fixed type identifiers. Catalog images and icons use stable media keys; Android resource IDs remain local to the client.

## Database migrations

Flyway is explicitly disabled in `application.properties`; Hibernate only validates
schema on startup. Development is baselined at V1 and has V2 through V16 applied; its existing
records and memberships were verified after migration. The immutable V1 captures
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
Migrate the database through V16 before running this backend.

V3 corrects Romanian media-key transliteration, removes the obsolete duplicate
`Ă` sign, and replaces resource integers with media keys throughout the catalog.
The retained `dactilema_a` illustration matches the text/voice sign collection;
the alphabet category uses that asset too. Letters are unique with case preserved,
and normalized category names are unique among siblings, including
root categories. Shared media keys remain allowed.

V4 removes the row revision counters from all nine entity tables. Applied
migration files remain unchanged so Flyway can validate existing installations.

V5 removes category display_order. V13 defines section ordering by UUID in application
code. Category lists, including favorites and filtered searches, use Java Stream sorting with Romanian alphabetical collation, ignoring case and using UUID
as a stable tie-breaker. /api/categories/filter returns only root categories
when no parent is supplied, or only the selected parent's direct children. Both
levels are alphabetically ordered. The obsolete category ordering index and
constraint are removed with the column; the sibling-name index supports parent
filtering after V14.

V6 removes category codes. Category navigation and direct-child filters use UUIDs;
the category filter accepts optional parentId and name; V14 removes sectionId.
Renaming a parent leaves its UUID, children, and media keys unchanged. V13 also
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

V12 renames `user_favorites` to `favorites` and drops `created_at`. Each row is
only a user/category UUID pair. Existing pairs, composite uniqueness and cascading
foreign keys are preserved; favorite lists continue to use alphabetical category order.

V13 removes section `display_order` and `code`. V15 replaces UUID-based section
ordering with the fixed declaration order of the API's Section enum.

The retired Text to Sign section was deleted directly as a catalog record change,
without a schema migration. It had no linked categories in development. Remaining
retained section IDs and all other records are preserved. Voice to Sign retains both typed
input and speech recognition unchanged.

V14 removes `categories.section_id` and replaces the composite parent foreign key
with a parent-only foreign key. Sibling-name uniqueness covers the entire catalog,
including roots; the database trigger still prevents hierarchy cycles. Categories
use UUID, name, parent UUID and media key only. `/api/categories/filter` returns
alphabetical roots when parentId is absent and direct children when it is supplied.

The six expression categories now sit under the `Expresii` parent inside Video.
The parent reuses the former Expressions section UUID; existing expression IDs,
media keys, descendant links and favorites are retained. The two remaining main
sections are Video and Voice to Sign. Category creation and updates need no sectionId.

Catalog record maintenance is separate from V14. On an existing V13 database,
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

V15 drops the unreferenced `sections` table. `GET /api/sections` remains a read-only,
database-independent menu in fixed order: VIDEO, then VOICE_TO_SIGN. Each response
entry has type, name, mediaKey and iconKey; section UUIDs and section mutation/detail
endpoints are removed. Menu definitions and ordering can only change in API code.
All remaining application records are preserved. Applied migration files remain immutable.
Section display names are Romanian: „Secțiune video” and „Voce – semn”. Enum
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
to return that user's favorite status. Favorite requests require a session, and the supplied user UUID must match
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
New and updated user passwords are hashed with BCrypt (`$2b$`, cost factor 12),
using a fresh random salt for each hash. User HTTP responses exclude passwords.
Public registration requires 15 to 72 characters and at most 72 UTF-8 bytes;
oversized values return HTTP 400. Existing BCrypt credentials remain valid.
Administrator account updates retain their existing validation rules. Stored credentials
use `passwordHash` in Java and `users.password_hash` in PostgreSQL. Request-only
`password` fields accept raw passwords; response mappings never copy stored hashes.

V16 renames the column and migrates legacy plaintext credentials to BCrypt (`$2b$`,
cost 12), preserving existing BCrypt hashes, user UUIDs, profiles, roles, schools,
and favorites. Invalid BCrypt values and legacy passwords over 72 UTF-8 bytes
abort the migration instead of being truncated. Apply V16 before starting this
backend; migrations and directory imports remain disabled during normal startup.

## Authentication

`POST /api/auth/register` accepts name, email, password, phone and schoolId. It
always assigns the existing USER role; callers cannot choose a role or user ID.
Emails are normalized and unique. Registration signs in the new account.
`POST /api/auth/login` verifies BCrypt credentials and rotates the session ID.
Failed login attempts are limited to five per IP/email pair in 15 minutes.
Registration is limited to 30 attempts per IP in 15 minutes. Limits reserve an
attempt before credential checks, so concurrent requests cannot bypass them.

`GET /api/auth/me` returns the authenticated account without its password.
`POST /api/auth/logout` invalidates the session. Sessions expire after 30 idle
minutes and are held in memory, so an API restart signs clients out. No schema
migration or account rewrite is required. Existing development records are unchanged.

Cookie authentication uses HttpOnly, SameSite=Strict cookies. Cookies are Secure
by default; production must use HTTPS. For local HTTP previews only, explicitly
set `SESSION_COOKIE_SECURE=false`. Fetch `GET /api/auth/csrf` first, retain its
JSESSIONID cookie, and send its
`token` in `X-CSRF-TOKEN` for every POST, PUT, PATCH and DELETE. Fetch a new token
after login or registration because authentication rotates it. Catalog and school
directory reads remain public; private account data and favorites are protected.

The Android app stores only an encrypted session cookie and validates it with
`/api/auth/me`. Local profile selection never establishes authentication.
Authentication integration tests use isolated H2 data and cover permissions,
CSRF, role assignment, duplicate accounts, session fixation protection, logout,
password verification, and login throttling. Deployment can scale to multiple
instances by replacing in-memory sessions and throttling with shared storage.

References: [Spring Security sessions](https://docs.spring.io/spring-security/reference/servlet/authentication/persistence.html),
[CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html),
and [Android Keystore](https://developer.android.com/privacy-and-security/keystore).

## Structure

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
