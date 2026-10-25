# Backend

Spring Boot 4 REST API (Java, Maven), with JPA, Spring Security, Spring Mail, Lombok, jjwt and Google GenAI.

## Package layout (`src/main/java/com/homie/`)
- `controller/`: REST endpoints (`AuthController` `/auth`, `RecipeController` `/recipes`, `RecipeManagementController` `/management/recipes`, `HealthController` `/health`). Request/response DTOs live in `controller/dto/`.
- `model/`: JPA entities (`AppUser`, `EmailVerificationToken`, `Recipe`, `Step`)
- `repository/`: Spring Data repositories
- `services/`: business logic and external integrations (`RecipeService`, `EmailVerificationService`, `MailService`, Gemini)
- `security/`: `ApplicationSecurityConfig` (filter chain, CORS), `DbUserDetailsService` (loads users from the DB), `ApplicationUserRole` / `ApplicationUserPermission` (roles and permissions)
- `jwt/`: `JwtService` (create/verify tokens, build cookies), `JwtCookieAuthFilter` (authenticates each request from the cookie)
- `db/Seeder.java`: seed data on startup (only with the `dev` profile)

Conventions:
- Keep controllers thin. Persistence goes in `repository`, logic in `services`.
- Never return entities that contain secrets. Users go out as `AuthDtos.UserResponse`.

## Security / auth flow
- Stateless JWT in an **HttpOnly cookie** (`access_token`, SameSite=Lax, 60 min). The frontend never reads the token.
  - `POST /auth/login` checks the credentials, then sets the cookie and returns the user. Unverified email → 403.
  - `POST /auth/register` creates the user with `emailVerified = false`, mails a confirmation link (`{app.frontend-url}/verify-email?token=…`) and returns 202 **without** a cookie. If the mail can't be sent, the user is deleted again (503).
  - `POST /auth/verify-email` `{ token }` marks the email as verified, then sets the cookie and returns the user (auto-login). Unknown/used/expired token → 400.
  - `POST /auth/resend-verification` `{ email }` sends a new link to an unverified account. Always 202, so it doesn't reveal which emails exist.
  - Tokens are 32 random bytes, one-time, valid `app.verification-token-hours` (24h). Only their SHA-256 hash is stored (`EmailVerificationToken`). A new link replaces the old one.
  - `DbUserDetailsService` marks unverified users as `disabled`, so Spring throws `DisabledException` on login.
  - `GET /auth/me` returns the current user (401 if not logged in). `POST /auth/logout` clears the cookie.
  - `JwtCookieAuthFilter` verifies the cookie on every request and sets the `SecurityContext` from the token's `authorities` claim.
- Config under `jwt.*`. Outside the `dev` profile, `JWT_SECRET` (base64, ≥256 bit) is required and the cookie is `Secure`.
- Access rules are defined only in `ApplicationSecurityConfig`. Use the role/permission enums, not string literals.
  - Roles: `USER` (`recipe:read`, `recipe:write`) and `ADMIN` (+ `recipe:admin`, needed for `/management/**`).
- CORS is configured in the `CorsConfigurationSource` bean for `http://localhost:5173`, with credentials allowed. Don't add `@CrossOrigin` to controllers.
- Unauthenticated → 401 and missing permission → 403, never a redirect. `/error` must stay `permitAll`, or 403/404 would turn into 401.
- CSRF is disabled for now. Enable `CookieCsrfTokenRepository` before a public deployment.
- Dev users (seeded in `dev`, already verified): `anna`/`password` (USER), `admin`/`password123` (ADMIN), `user`/`password` (USER, owns 3 private recipes with steps).

## Mail
- Sent via `JavaMailSender` (`spring.mail.*`). Outside `dev`, set `MAIL_HOST`, `MAIL_PORT` (587), `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` and `FRONTEND_URL`.
- In `dev`, mails go to **Mailpit** on `localhost:1025` (no auth/TLS). Its inbox is at http://localhost:8025. Docker Compose starts it. Locally: `docker run -p 1025:1025 -p 8025:8025 axllent/mailpit`.
- Tests mock `JavaMailSender` with `@MockitoBean` (see `EmailVerificationTests`).

## Profiles
- `application.yml`: base config for all environments (secure defaults, no seeding, H2 console off).
- `application-dev.yml` (`dev` profile): dev JWT secret, non-secure cookie, H2 console on, Mailpit as SMTP, frontend URL `http://localhost:5173`, and the `Seeder` runs (`@Profile("dev")`).
- Docker dev sets `SPRING_PROFILES_ACTIVE=dev`. Tests use `@ActiveProfiles("dev")` with an in-memory DB.

## Database
- H2 file DB at `/tmp/homiedb`. `ddl-auto: create-drop` recreates the schema on every start. In `dev` the data comes from `Seeder.java`, otherwise the DB starts empty.
- Tables: `app_user` (`user` is reserved in H2, has `email_verified`), `email_verification_token`, `recipe` (`owner_id` → `app_user`, `shared`), `step`.
- A user sees their own recipes plus all `shared` ones (`RecipeRepository.findVisibleFor`).
- `owner` is also the creator ("by …" in the UI). `POST /recipes` (`RecipeDtos.CreateRecipeRequest`, needs `recipe:write`) always sets it to the logged-in user, never from the request. `RecipeService.create` drops blank ingredients/steps and numbers the steps in order. Missing name / negative time / no ingredient → 400, success → 201 with the recipe.
- H2 console (dev only): `http://localhost:8080/h2-console`

## Commands
- `./mvnw compile`
- `./mvnw test`
- `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (local dev with seed data)
