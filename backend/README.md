# Homie Backend

Spring Boot 4 REST API for Homie, on http://localhost:8080. Architecture, auth flow and API details are in [CLAUDE.md](CLAUDE.md).

## Running

### Docker (recommended)
From the repo root:

```sh
docker compose -f docker-compose.dev.yml up
```

This starts the backend, the frontend (http://localhost:5173) and Mailpit, with the `dev` profile active.

- `src/` and `pom.xml` are mounted, and DevTools restarts the app when code changes.
- After changing `pom.xml`: `docker compose -f docker-compose.dev.yml restart backend`
- After changing `Dockerfile.dev`: add `--build` to `up`.
- Logs: `docker logs -f homie-backend`

### Locally
Needs a JDK. From `backend/`:

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev     # Windows: mvnw.cmd ...
docker run -p 1025:1025 -p 8025:8025 axllent/mailpit       # for registration mails
```

### Good to know
- `GOOGLE_API_KEY` enables the Gemini nutrition suggestions. Without it, recipes still save, but the macros stay empty.
- Dev users (seeded): `anna`/`password`, `admin`/`password123` (admin), `user`/`password`.
- Registration mails land in Mailpit: http://localhost:8025
- Health check: http://localhost:8080/health

## Database
H2 console (only with the `dev` profile): http://localhost:8080/h2-console

| Field    | Value                      |
|----------|----------------------------|
| JDBC URL | `jdbc:h2:file:/tmp/homiedb` |
| User     | `sa`                       |
| Password | *(empty)*                  |

- The schema uses `create-drop`, so every start wipes the DB and the `Seeder` refills it. Don't keep data you care about in it.
- Remote access (`web-allow-others`) is on in `dev`, because in Docker the browser's requests count as remote.
- Since Boot 4, the console needs the `spring-boot-h2console` dependency, which is already in `pom.xml`.

## Tests

```sh
./mvnw test                       # all tests
./mvnw test -Dtest=MealPlanTests  # one class
```

Without a local JDK, run them in the running container: `docker exec homie-backend ./mvnw test`

Tests use an in-memory H2 database and mock the mail sender and Gemini, so they don't need Mailpit, an API key or the running app.
