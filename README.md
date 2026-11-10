# OpenHands Learning API

REST API for the OpenHands Learning Android application.

## Build and test

Install JDK 17 or newer and set `JAVA_HOME`. The checked-in Maven Wrapper downloads
Maven 3.10.0 and verifies its checksum. No global Maven installation is needed.

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
compatible Spring Framework, Jackson 3, PostgreSQL driver, Lombok, test libraries,
and Maven plugin versions. The MVC starter uses Boot 4's `spring-boot-starter-webmvc`.

Release sources: [Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html),
[Maven](https://maven.apache.org/download.cgi), and [springdoc](https://springdoc.org/).
