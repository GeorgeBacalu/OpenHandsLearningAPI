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

Tests cover persistence, optimistic locking, foreign keys, HTTP errors, BCrypt,
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
HTTP IDs are UUID strings. Resource image/icon numbers remain integers.

## Database migrations

Flyway 13.12.1 uses `src/main/resources/db/migration/V1__initial_schema.sql` to
create the current PostgreSQL schema, including UUID defaults, primary keys,
foreign keys, and catalog ordering. It contains no records. The earlier manual
UUID conversion is superseded by this initial schema migration.

Flyway is disabled by default (`FLYWAY_ENABLED=false`) so starting the API against
the existing development database does not create a history table or change its
schema or data. Automatic baselining is disabled. Existing databases are not
enrolled in Flyway by this change.

To initialize a separate, empty PostgreSQL database, set its connection and enable
Flyway for that process before starting the API:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/open-hands-learning-new"
$env:FLYWAY_ENABLED = "true"
.\mvnw.cmd spring-boot:run
```

Flyway applies V1 once and records its checksum; Hibernate then validates the
schema. Do not edit an applied migration; add V2 for subsequent schema changes.
The H2 tests retain Hibernate schema creation and their separate minimal fixtures.

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
each other. Service transactions wrap complete use cases, JPA uses optimistic
locking, and MapStruct-generated DTO mapping uses scalar reference IDs without
recursive entity loading. ArchUnit tests enforce these boundaries.

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
