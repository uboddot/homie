# Homie

## Purpose
Homie is a recipe app. Users keep their own recipes (a personal collection) and share recipes with others.

## Architecture
```
frontend/  React SPA (TypeScript, Vite)   → http://localhost:5173
    │  JSON over HTTP (fetch, credentials: 'include')
    ▼
backend/   Spring Boot REST API (Java)     → http://localhost:8080
    │  JPA
    ▼
PostgreSQL (schema via Flyway)
```

Staging runs on Railway as 3 services (see [DEPLOY.md](DEPLOY.md)): the frontend's nginx is the only public one and proxies `/api/*` to the backend over the private network, so browser and API share one origin. Postgres is Railway's managed one.

- Auth: stateless JWT in an HttpOnly cookie. Users are stored in the DB (`app_user`), and login/register run through `/auth/*`. See backend/frontend CLAUDE.md for details.
- CORS for the frontend origin is set up centrally in the backend security config.

Details:
- Backend: [backend/CLAUDE.md](backend/CLAUDE.md)
- Frontend: [frontend/CLAUDE.md](frontend/CLAUDE.md)

## Running
- Docker (frontend, backend, Postgres, Mailpit): `docker compose -f docker-compose.dev.yml up`
  - `src/` and `pom.xml` of the backend are mounted. After changing dependencies, restart the backend container. After changing the Dockerfile, use `--build`.
  - Maven and its dependencies are baked into the image and cached in the `homie-m2` volume (compiled classes in `homie-backend-target`), so only the first start downloads them. To reset the cache: `docker compose -f docker-compose.dev.yml down` and `docker volume rm homie_homie-m2 homie_homie-backend-target`.
  - Dev data lives in the `homie-pg-data` volume. Reset: `docker compose -f docker-compose.dev.yml down -v`.
- Prod images locally (like on Railway): `docker compose -f docker-compose.staging.yml up --build` → http://localhost:8081
- Locally:
  - Postgres: `docker run -p 5432:5432 -e POSTGRES_USER=homie -e POSTGRES_PASSWORD=homie postgres:17-alpine`
  - Backend: `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`. The `dev` profile seeds the dev users and recipes; Docker sets it automatically.
  - Frontend: `cd frontend && npm run dev`
- The Gemini integration needs the `GOOGLE_API_KEY` env variable.
- Registration sends a confirmation mail. In dev it lands in Mailpit (inbox: http://localhost:8025). Docker starts it; for local runs, use `docker run -p 1025:1025 -p 8025:8025 axllent/mailpit`.

## CI / deployment
- `.github/workflows/backend.yml` and `frontend.yml`: tests on PRs, and on `main` pushes `ghcr.io/uboddot/homie-<part>:latest` + `:sha-…`. Path filters, so only the changed part is built.
- Prod images: `backend/Dockerfile`, `frontend/Dockerfile` (+ `frontend/nginx/default.conf.template`). `Dockerfile.dev` is for local dev only.
- Railway setup and env variables: [DEPLOY.md](DEPLOY.md).
