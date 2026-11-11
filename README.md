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
schema on startup. Development is baselined at V1 and has V2 through V4 applied; its existing
records and memberships were verified after migration. The immutable V1 captures
the previous UUID schema. V2 adds:

- Per-user favorites; existing shared favorites are copied to each existing user.
- Institutions representing schools, separate classrooms, and optional user classroom membership.
- Unique normalized account emails and school/classroom names, required values, and supported role/feedback values.
- Stable catalog `code`, `mediaKey`, and section `iconKey` fields, independent of translated display names.
- Query indexes, matching-section parent relationships, and a serialized database trigger preventing hierarchy cycles.

V2 transforms existing records and memberships; it seeds no users or catalog
content. Space characters remain valid sign entries. New empty databases contain
no application records after V1 through V4. Back up and rehearse upgrades on a copy
before deploying these branches. Existing duplicate account emails or ambiguous
classroom names must be resolved explicitly; migration failures roll back V2.
Migrate the database through V4 before running this backend.

V3 corrects Romanian media-key transliteration, removes the obsolete duplicate
`Ă` sign, and replaces resource integers with media keys throughout the catalog.
The retained `dactilema_a` illustration matches the text/voice sign collection;
the alphabet category uses that asset too. Letters are unique with case preserved,
and normalized category names are unique within their section and parent, including
root categories. Shared media keys remain allowed.

Feedback descriptions and notification messages use PostgreSQL `text`, with a
10,000-character API limit. Their event times use `timestamptz` and Java `Instant`;
V3 interprets existing local timestamps in `Europe/Bucharest`, the application
timezone, and preserves those instants. New responses use UTC ISO timestamps.
Notification lists accept `receiverId`, feedback lists accept `userId`, and both
accept `page` (default 0) and `size` (default 50, maximum 100), ordered by newest
event then UUID. Existing user/time indexes support the filtered reads.

V4 removes the row revision counters from all nine entity tables. Applied
migration files remain unchanged so Flyway can validate existing installations.

For a separate empty PostgreSQL database:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/open-hands-learning-new"
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--spring.flyway.enabled=true'
```

For an audited copy already matching V1 with no Flyway history, explicitly
baseline it at version 1 before applying subsequent migrations. Automatic baselining remains disabled
in the application's default configuration. Never edit applied migrations.

Regular tests use Hibernate-created H2 schemas and two records per table. The
Flyway baseline test checks V1 creation and repeat runs on H2. V2 is PostgreSQL
specific because its hierarchy trigger serializes concurrent tree changes. Its
upgrade and record preservation were verified using a temporary database copy;
there is no PostgreSQL provisioning or rehearsal configuration in the project.

Favorites endpoints require `userId`; a category read accepts optional `userId`
to return that user's favorite status. Profile selection is not authentication:
the existing API remains public and needs an authentication layer before serving
private accounts in production. Catalog filters accept `parentCode` and
`sectionCode` (default `VIDEO`), while UUID filters remain supported. Clients can
rename labels without changing codes or media keys. Codes are immutable through
the API. Classroom CRUD is available at `/api/classrooms`.

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
- `adapter/config`: dependency wiring, service transaction boundaries, password hashing, and clock configuration.

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
