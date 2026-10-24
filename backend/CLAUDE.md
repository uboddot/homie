# Backend

Spring Boot 4 REST API (Java, Maven), with JPA, Spring Security, Lombok, jjwt and Google GenAI.

## Package layout (`src/main/java/com/homie/`)
- `controller/`: REST endpoints (`AuthController` `/auth`, `RecipeController` `/recipes`, `RecipeManagementController` `/management/recipes`, `HealthController` `/health`). Request/response DTOs live in `controller/dto/`.
- `model/`: JPA entities (`AppUser`, `Recipe`, `Step`)
- `repository/`: Spring Data repositories
- `services/`: business logic and external integrations (Gemini)
- `security/`: `ApplicationSecurityConfig` (filter chain, CORS), `DbUserDetailsService` (loads users from the DB), `ApplicationUserRole` / `ApplicationUserPermission` (roles and permissions)
- `jwt/`: `JwtService` (create/verify tokens, build cookies), `JwtCookieAuthFilter` (authenticates each request from the cookie)
- `db/Seeder.java`: seed data on startup (only with the `dev` profile)

Conventions:
- Keep controllers thin. Persistence goes in `repository`, logic in `services`.
- Never return entities that contain secrets. Users go out as `AuthDtos.UserResponse`.

## Security / auth flow
- Stateless JWT in an **HttpOnly cookie** (`access_token`, SameSite=Lax, 60 min). The frontend never reads the token.
  - `POST /auth/login` and `POST /auth/register` check the credentials, then set the cookie and return the user.
  - `GET /auth/me` returns the current user (401 if not logged in). `POST /auth/logout` clears the cookie.
  - `JwtCookieAuthFilter` verifies the cookie on every request and sets the `SecurityContext` from the token's `authorities` claim.
- Config under `jwt.*`. Outside the `dev` profile, `JWT_SECRET` (base64, ≥256 bit) is required and the cookie is `Secure`.
- Access rules are defined only in `ApplicationSecurityConfig`. Use the role/permission enums, not string literals.
  - Roles: `USER` (`recipe:read`, `recipe:write`) and `ADMIN` (+ `recipe:admin`, needed for `/management/**`).
- CORS is configured in the `CorsConfigurationSource` bean for `http://localhost:5173`, with credentials allowed. Don't add `@CrossOrigin` to controllers.
- Unauthenticated → 401 and missing permission → 403, never a redirect. `/error` must stay `permitAll`, or 403/404 would turn into 401.
- CSRF is disabled for now. Enable `CookieCsrfTokenRepository` before a public deployment.
- Dev users (seeded in `dev`): `anna`/`password` (USER), `admin`/`password123` (ADMIN), `user`/`password` (USER, owns 3 private recipes with steps).

## Profiles
- `application.yml`: base config for all environments (secure defaults, no seeding, H2 console off).
- `application-dev.yml` (`dev` profile): dev JWT secret, non-secure cookie, H2 console on, and the `Seeder` runs (`@Profile("dev")`).
- Docker dev sets `SPRING_PROFILES_ACTIVE=dev`. Tests use `@ActiveProfiles("dev")` with an in-memory DB.

## Database
- H2 file DB at `/tmp/homiedb`. `ddl-auto: create-drop` recreates the schema on every start. In `dev` the data comes from `Seeder.java`, otherwise the DB starts empty.
- Tables: `app_user` (`user` is reserved in H2), `recipe` (`owner_id` → `app_user`, `shared`), `step`.
- A user sees their own recipes plus all `shared` ones (`RecipeRepository.findVisibleFor`).
- H2 console (dev only): `http://localhost:8080/h2-console`

## Commands
- `./mvnw compile`
- `./mvnw test`
- `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (local dev with seed data)
