# OpenHandsLearning

REST API for the OpenHandsLearning Android application.

## Build and test

Install JDK 25 and set `JAVA_HOME` to that installation. Java 25 is required to
build and run the API; JDK 17 is not required. The Maven Wrapper downloads Maven
3.10.0 and verifies its checksum. No global Maven installation or wrapper JAR is
needed.

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
HTTP IDs are UUID strings. Catalog images and icons use stable media keys; Android resource IDs remain local to the client.

## Database migrations

Flyway is explicitly disabled in `application.properties`; Hibernate only validates
schema on startup. Development is baselined at V1 and has V2 through V14 applied; its existing
records and memberships were verified after migration. The immutable V1 captures
the previous UUID schema. V2 adds:

- Per-user favorites; existing shared favorites are copied to each existing user.
- Schools and normalized user membership.
- Unique normalized account emails, required values, and supported role values.
- Stable catalog `code`, `mediaKey`, and section `iconKey` fields, independent of translated display names.
- Query indexes, matching-section parent relationships, and a serialized database trigger preventing hierarchy cycles.

V2 transforms existing records and memberships; it seeds no users or catalog
content. Space characters remain valid sign entries. New empty databases contain
no application records after V1 through V7. Back up and rehearse upgrades on a copy
before deploying these branches. Existing duplicate account emails must be resolved explicitly; migration failures
roll back V2.
Migrate the database through V14 before running this backend.

V3 corrects Romanian media-key transliteration, removes the obsolete duplicate
`Ă` sign, and replaces resource integers with media keys throughout the catalog.
The retained `dactilema_a` illustration matches the text/voice sign collection;
the alphabet category uses that asset too. Letters are unique with case preserved,
and normalized category names are unique within their section and parent, including
root categories. Shared media keys remain allowed.

V4 removes the row revision counters from all nine entity tables. Applied
migration files remain unchanged so Flyway can validate existing installations.

V5 removes category display_order. V13 defines section ordering by UUID in application
code. Category lists, including favorites and filtered searches, use Java Stream sorting with Romanian alphabetical collation, ignoring case and using UUID
as a stable tie-breaker. /api/categories/filter returns only root categories
when no parent is supplied, or only the selected parent's direct children. Both
levels are alphabetically ordered. The obsolete category ordering index and
constraint are removed with the column; the sibling-name index supports parent
and section filtering.

V6 removes category codes. Category navigation and direct-child filters use UUIDs;
the category filter requires sectionId and accepts optional parentId and name.
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

V13 removes section `display_order` and `code`. The section service uses Stream
sorting with a fixed UUID list: Video → Voice to Sign → Expressions (after V14).
Other section UUIDs follow in UUID-string order. Renaming a section does not change
its position or Android screen; existing IDs, media keys and category links are preserved.
Changing the fixed menu order requires a code change.

V14 removes the retired Text to Sign section by its canonical UUID. It has no
linked categories in development. Remaining section IDs and all other records
are preserved; category references prevent deleting a section that still has content.
Voice to Sign retains both typed input and speech recognition unchanged.

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
to return that user's favorite status. Profile selection is not authentication:
the existing API remains public and needs an authentication layer before serving
private accounts in production. Catalog filters use the selected parent UUID and section UUID. Clients can
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
Passwords must contain 8 to 72 characters and fit within 72 UTF-8 bytes; oversized
values return HTTP 400. Existing credentials are not silently rewritten; password
updates store BCrypt.

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
