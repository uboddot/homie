# Homie Backend

Spring Boot 4 REST API for Homie, on http://localhost:8080. Architecture, auth flow and API details are in [CLAUDE.md](CLAUDE.md).

## Running

### Docker (recommended)
From the repo root:

```sh
docker compose -f docker-compose.dev.yml up
```

This starts the backend, the frontend (http://localhost:5173), Postgres and Mailpit, with the `dev` profile active.

- `src/` and `pom.xml` are mounted, and DevTools restarts the app when code changes.
- After changing `pom.xml`: `docker compose -f docker-compose.dev.yml restart backend`
- After changing `Dockerfile.dev`: add `--build` to `up`.
- Logs: `docker logs -f homie-backend`

### Locally
Needs a JDK and Docker (for Postgres). From `backend/`:

```sh
docker run -p 5432:5432 -e POSTGRES_USER=homie -e POSTGRES_PASSWORD=homie postgres:17-alpine
docker run -p 1025:1025 -p 8025:8025 axllent/mailpit       # for registration mails
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev     # Windows: mvnw.cmd ...
```

### Good to know
- `GOOGLE_API_KEY` enables the Gemini nutrition suggestions. Without it, recipes still save, but the macros stay empty.
- Dev users (seeded): `anna`/`password`, `admin`/`password123` (admin), `user`/`password`.
- Registration mails land in Mailpit: http://localhost:8025
- Health check: http://localhost:8080/health

## Database
Postgres, with the schema managed by Flyway (`src/main/resources/db/migration`).

| Field    | Value (dev)                              |
|----------|------------------------------------------|
| JDBC URL | `jdbc:postgresql://localhost:5432/homie` |
| User     | `homie`                                  |
| Password | `homie`                                  |

- Connect with any SQL client, or `docker exec -it homie-postgres psql -U homie`.
- The data is kept across restarts (volume `homie-pg-data`). The `Seeder` only fills an empty DB. Reset: `docker compose -f docker-compose.dev.yml down -v`.
- **Changing an entity?** Add a new migration `V<next>__what_changed.sql`. Never edit a migration that has already run. Hibernate only validates the schema (`ddl-auto: validate`), so a missing migration fails at startup.

## Tests

```sh
./mvnw test                       # all tests
./mvnw test -Dtest=MealPlanTests  # one class
```

Tests start a throwaway Postgres per test class with Testcontainers (`jdbc:tc:postgresql:…` URLs), so they need Docker running. They mock the mail sender and Gemini, so they don't need Mailpit, an API key or the running app.
