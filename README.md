# Open Hands Learning — API

The REST backend for [Open Hands Learning for Android](https://github.com/GeorgeBacalu/OpenHandsLearningUI).
It provides account registration and authentication, a hierarchical sign-language
catalog, user favorites, a Romanian school directory, and media administration.

The service uses **Java 25**, **Spring Boot 4.1.2**, Spring Security with bearer
tokens, Spring Data JPA, PostgreSQL, Flyway migrations, and S3-compatible storage.

## Hosted application

- [API description](https://openhandslearning-api.onrender.com/openapi.json)
- [Public county directory](https://openhandslearning-api.onrender.com/api/counties)
- [Android APK releases](https://github.com/GeorgeBacalu/OpenHandsLearningUI/releases/latest)
- [Deployment and infrastructure guide](DEPLOYMENT.md)

The API runs on Render. The production PostgreSQL database and media bucket are
in Neon; the project, database, and bucket are named `open-hands-learning`.
The first request after an idle period may take longer while the API starts.

## Run locally

Install **JDK 25** and **PostgreSQL 18**, and set `JAVA_HOME` to your JDK.
The included Maven wrapper supplies Maven **3.10.0**. Commands below use
PowerShell from this repository's root; on macOS/Linux use `./mvnw` and your
shell's environment-variable syntax.

1. Create a local development database, using your local PostgreSQL administrator:

   ```powershell
   createdb -U postgres open-hands-learning
   ```

2. Set the database credentials in your shell. Replace the placeholders with
   credentials for your local database:

   ```powershell
   $env:POSTGRES_USERNAME = "postgres"
   $env:POSTGRES_PASSWORD = "<local database password>"
   ```

3. For a new database, apply the versioned schema migrations at the first startup:

   ```powershell
   .\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--spring.flyway.enabled=true'
   ```

   The default `dev` profile connects to
   `jdbc:postgresql://localhost:5432/open-hands-learning` and listens on port
   **8080**. Hibernate validates the schema. Flyway is disabled by default in
   development, so enable it as above when initializing the database or applying
   new migrations. For normal subsequent startups:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. Open [the local API description](http://localhost:8080/openapi.json).

A fresh database needs catalog and school-directory data; schema migrations do
not copy the hosted database. The optional government school-directory importer
is enabled with `--app.school-directory.import=true`. It downloads and imports
directory data into the configured database. Deployment import details are in
[DEPLOYMENT.md](DEPLOYMENT.md).

Local media administration uses AWS S3. Configure `AWS_ACCESS_KEY` and
`AWS_SECRET_ACCESS`, or leave both unset to use the AWS SDK's normal credential
provider chain. Storage credentials are required for storage operations, not for
the Android app to display publicly readable media. Keep credentials out of Git.

## API overview

The full contract is in [openapi.json](src/main/resources/static/openapi.json),
also served at `/openapi.json`.

| Routes | Purpose | Access |
| --- | --- | --- |
| `POST /api/auth/register`, `POST /api/auth/login` | Create an account and obtain a token | Public |
| `/api/auth/me`, `/api/auth/password`, `/api/auth/logout` | Account and session management | Signed in |
| `/api/categories`, `/api/categories/filter` | Browse and search the catalog | Signed in for reads; administrator for catalog writes |
| `/api/categories/favorites/**` | Manage favorites | Signed in; ownership checks apply |
| `GET /api/counties`, `GET /api/cities`, `GET /api/schools/search` | School selection | Public |
| `/api/media/{type}` | Read and administer media | Public reads; administrator writes |
| `/api/users/**` | User administration | Administrator |
| `/api/schools`, `/api/schools/{id}` | School records | Signed in for reads; administrator for writes |

Send the token returned by login or registration as
`Authorization: Bearer <accessToken>` for protected requests. Tokens expire after
30 minutes. Signing keys are generated per API process, so restarting the service
also invalidates previously issued tokens; sign in again when required.

## Profiles and media paths

| Profile | Database | Storage |
| --- | --- | --- |
| `dev` (default) | Local PostgreSQL `open-hands-learning` | AWS S3 `open-hands-learning`, `us-east-1` |
| `prod` | `DATABASE_JDBC_URL` | Neon bucket `open-hands-learning`, `eu-central-1` |
| `render` | Includes `prod`; uses Render's `PORT` | Includes `prod` |

Production requires `DATABASE_JDBC_URL`, `POSTGRES_USERNAME`, `POSTGRES_PASSWORD`,
`NEON_STORAGE_ACCESS_KEY_ID`, and `NEON_STORAGE_SECRET_ACCESS_KEY`. Flyway runs
automatically in production. See [DEPLOYMENT.md](DEPLOYMENT.md) for deployment
configuration and storage permissions.

Both storage providers use this structure directly at the bucket root:

```text
categories/<category path>/main.webp
categories/<category path>/main.mp4
voice/<asset>.webp
```

There is no `dev/` or `prod/` folder prefix. Production media URLs start with
`https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/open-hands-learning/`.
Development uses `https://open-hands-learning.s3.us-east-1.amazonaws.com/`.
Migrations V18–V20 update existing catalog URLs for the root paths and renamed
Neon bucket. Verify that media exists at its destination before deploying a URL
change.

## Build and test

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

The packaged application is `target/OpenHandsLearningAPI-0.0.1-SNAPSHOT.jar`.
Tests use their own fixtures and H2 where configured; ordinary test runs do not
require live media-storage credentials. The opt-in `MediaStorageSmokeTest`
contacts real storage and requires explicit configuration, as described in
[DEPLOYMENT.md](DEPLOYMENT.md).

## Source layout

- `domain/`: core models and domain errors.
- `application/`: use cases, services, DTOs, mappers, and ports.
- `adapter/`: web controllers, security, persistence, directory import, and storage.
- `src/main/resources/db/migration/`: versioned PostgreSQL migrations.
- `infrastructure/`: storage policies and deployment/maintenance scripts.
- `Dockerfile` and `render.yaml`: container and Render deployment configuration.
