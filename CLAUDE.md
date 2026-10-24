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
H2 database (file-based, dev only)
```

- Auth: stateless JWT in an HttpOnly cookie. Users are stored in the DB (`app_user`), and login/register run through `/auth/*`. See backend/frontend CLAUDE.md for details.
- CORS for the frontend origin is set up centrally in the backend security config.

Details:
- Backend: [backend/CLAUDE.md](backend/CLAUDE.md)
- Frontend: [frontend/CLAUDE.md](frontend/CLAUDE.md)

## Running
- Docker (both services): `docker compose -f docker-compose.dev.yml up`
- Locally:
  - Backend: `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`. The `dev` profile seeds the dev users and recipes; Docker sets it automatically.
  - Frontend: `cd frontend && npm run dev`
- The Gemini integration needs the `GOOGLE_API_KEY` env variable.
