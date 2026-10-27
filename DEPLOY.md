# Deploying to Railway (staging)

Three Railway services (fits the free tier):

```
browser ──https──▶ frontend (nginx, public domain)
                     ├─ /        static SPA
                     └─ /api/*   ──private network──▶ backend:8080 (Spring Boot, staging profile)
                                                         └──▶ Postgres (Railway managed)
```

The browser only ever talks to the frontend's domain, so the `SameSite=Lax` auth cookie works and CORS isn't needed.
Images come from GHCR and are built by `.github/workflows/backend.yml` / `frontend.yml` on every push to `main`
that touches that part.

## One-time setup

### 1. GHCR
After the first push to `main`, both packages appear under github.com/uboddot → Packages.
For each of `homie-backend` and `homie-frontend`: *Package settings → Change visibility → Public*,
so Railway can pull them without registry credentials.

### 2. Railway project
Create an empty project, then add:

**Postgres:** *New → Database → PostgreSQL*. Keep the default name `Postgres`, because the variable references below use it.

**backend:** *New → Docker Image → `ghcr.io/uboddot/homie-backend:latest`*, name it `backend`. Don't give it a public domain. Variables:

| Variable | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `staging` |
| `PORT` | `8080` |
| `PGHOST` | `${{Postgres.PGHOST}}` |
| `PGPORT` | `${{Postgres.PGPORT}}` |
| `PGDATABASE` | `${{Postgres.PGDATABASE}}` |
| `PGUSER` | `${{Postgres.PGUSER}}` |
| `PGPASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `JWT_SECRET` | output of `openssl rand -base64 32` (never reuse the dev one) |
| `FRONTEND_URL` | `https://<frontend domain>` (for the link in the verification mail) |
| `RESEND_API_KEY` | API key from resend.com (Railway's free/hobby plans block SMTP) |
| `MAIL_FROM` | a sender on a domain verified in Resend, or `onboarding@resend.dev` for first tests (that one only delivers to your own Resend account address) |
| `GOOGLE_API_KEY` | Gemini key (optional: without it the nutrition suggestions stay empty) |

Settings → Healthcheck path: `/health`.

**frontend:** *New → Docker Image → `ghcr.io/uboddot/homie-frontend:latest`*, name it `frontend`. Settings → Networking → *Generate domain* (target port 80 or whatever Railway detects from `PORT`). Variables:

| Variable | Value |
|---|---|
| `BACKEND_URL` | `http://${{backend.RAILWAY_PRIVATE_DOMAIN}}:8080` |

Settings → Healthcheck path: `/`.

### 3. Automatic redeploys (optional)
Railway doesn't redeploy when `:latest` changes. To let CI do it:
- Railway project → Settings → Tokens → create a **project token** for the staging environment.
- GitHub repo → Settings → Secrets and variables → Actions → secret `RAILWAY_TOKEN`.
- If the services aren't named `backend` / `frontend`, set the repository variables `RAILWAY_SERVICE_BACKEND` / `RAILWAY_SERVICE_FRONTEND`.

Without the token, redeploy by hand in the Railway UI after CI has pushed.

## Database migrations
Flyway runs on backend startup and applies any new `backend/src/main/resources/db/migration/V<n>__*.sql`.
A failing migration stops the backend from starting, and the previous deployment keeps running.
Rolling back an image doesn't roll back the schema, so write migrations that the previous version can live with
(add columns before using them, drop them only a release later).

## Checking a deploy
- Backend logs: `Successfully applied N migrations` (or `Schema "public" is up to date`), then `Started HomieApplication`.
- `https://<frontend domain>/api/health` → `ok`.
- Register, click the link in the mail, and you're logged in.

To try the same setup locally first: `docker compose -f docker-compose.staging.yml up --build` → http://localhost:8081 (mails at http://localhost:8025).
