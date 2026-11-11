# OpenHands Learning API

REST API for the OpenHands Learning Android application.

## Build and test

Install JDK 25 and set `JAVA_HOME` to that installation. Compilation and tests use
Java 25; Maven Enforcer rejects other JDK versions. The checked-in Maven Wrapper
downloads Maven 3.10.0 and verifies its checksum. The script-only wrapper needs no
checked-in wrapper JAR or global Maven installation.

```powershell
.\mvnw.cmd clean verify
```

On macOS/Linux, use `./mvnw clean verify`. Tests use an isolated H2 database in
PostgreSQL compatibility mode and run the application's schema. HTTP integration
tests verify JSON responses and OpenAPI generation.

## Run locally

Provide PostgreSQL and configure `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` for your database.

```powershell
.\mvnw.cmd spring-boot:run
```

Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`, and the
OpenAPI document is at `http://localhost:8080/v3/api-docs`.

## Dependency baseline

Updated against stable releases on 2026-11-11: Spring Boot 4.1.2, Maven 3.10.0,
Maven Wrapper 3.3.4, ModelMapper 3.2.8, and springdoc 3.2.0. Spring Boot manages the
compatible Spring Framework, Jackson 3, PostgreSQL driver, and Lombok versions.
Explicit overrides update H2 to 2.5.252, JUnit Jupiter to 6.1.3, Mockito to 5.24.0,
Maven Compiler to 3.16.0, Surefire/Failsafe to 3.6.1, Install/Deploy to 3.2.0, and
Enforcer to 3.7.0. The MVC starter uses Boot 4's `spring-boot-starter-webmvc`.
JDK 25 is required to build and run the API; JDK 17 is not required.

The parent manages Spring's compatible dependency set. Version overrides in
`pom.xml` keep the explicitly upgraded components current. The Maven Wrapper
version, distribution URL, and checksum live in `.mvn/wrapper/maven-wrapper.properties`.
Spring Boot infers the JDBC driver from the database URL and uses its default
`schema.sql` and `data.sql` locations. Tests use the production schema with an
isolated H2 database and skip production seed data.

Release sources: [Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html),
[Maven](https://maven.apache.org/download.cgi), and [springdoc](https://springdoc.org/).
