# Backend

Spring Boot 4 REST API (Java, Maven), with JPA, Spring Security, Spring Mail, Lombok, jjwt and Google GenAI.

## Package layout (`src/main/java/com/homie/`)
- `controller/`: REST endpoints (`AuthController` `/auth`, `RecipeController` `/recipes`, `RecipeManagementController` `/management/recipes`, `MealPlanController` `/meal-plan`, `SettingsController` `/settings`, `HealthController` `/health`). Request/response DTOs live in `controller/dto/`.
- `model/`: JPA entities (`AppUser`, `EmailVerificationToken`, `Recipe`, `Step`, `MealPlanEntry` + enum `MealSlot`, `UserSettings` + embeddable `MealTarget`)
- `repository/`: Spring Data repositories
- `services/`: business logic and external integrations (`RecipeService`, `MealPlanService`, `UserSettingsService`, `EmailVerificationService`, `MailService`, Gemini)
- `security/`: `ApplicationSecurityConfig` (filter chain, CORS), `DbUserDetailsService` (loads users from the DB), `ApplicationUserRole` / `ApplicationUserPermission` (roles and permissions)
- `jwt/`: `JwtService` (create/verify tokens, build cookies), `JwtCookieAuthFilter` (authenticates each request from the cookie)
- `nutrition/`: self-contained module for the macros per portion (entities, repositories and services in one package), see "Nutrition" below
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
- Dev users (seeded in `dev`, already verified): `anna`/`password` (USER), `admin`/`password123` (ADMIN), `user`/`password` (USER, owns 3 private recipes with steps and a few meal plan entries around today).

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
- Tables: `app_user` (`user` is reserved in H2, has `email_verified`), `email_verification_token`, `recipe` (`owner_id` → `app_user`, `shared`), `recipe_ingredient` (`@ElementCollection` of the `Ingredient` embeddable, ordered by `position`), `step`.
- An ingredient is `{ amount, unit, name }`: `amount` a decimal (`BigDecimal`), `unit` an `IngredientUnit` enum constant (`G`, `KG`, `DL`, `TSP`, `PINCH`, `PIECE`, …). Both are optional ("salt"), `name` is required.
- `portions` (int ≥ 1) says how many portions the ingredient amounts are for; the frontend scales from it. Missing in a request → 4 (`RecipeService.DEFAULT_PORTIONS`).
- A user sees their own recipes plus all `shared` ones (`RecipeRepository.findVisibleFor`).
- `owner` is also the creator ("by …" in the UI). `POST /recipes` (`RecipeDtos.CreateRecipeRequest`, needs `recipe:write`) always sets it to the logged-in user, never from the request. `RecipeService.create` drops blank ingredients/steps and numbers the steps in order. Missing name / negative time / portions < 1 / no ingredient / amount ≤ 0 / unit without amount / amount or unit without a name / unknown unit → 400. A valid cookie whose user is no longer in the DB (e.g. after a restart recreated it) → 401. Success → 201 with the recipe.
- `PUT /recipes/{id}` (same body, needs `recipe:write`) overwrites all fields and replaces the steps (`RecipeService.update`). Only the owner may edit: unknown id → 404, someone else's recipe → 403, same 400 validation as create, success → 200 with the recipe.
- H2 console (dev only): `http://localhost:8080/h2-console`

## Meal plan
- Table `meal_plan_entry` (`MealPlanEntry`): `user_id`, `plan_date` (`date` is reserved in H2), `slot` (`MealSlot`: `BREAKFAST`, `LUNCH`, `DINNER`), `recipe_id`. At most one entry per user, date and slot (unique constraint). Past days are what was eaten, future ones what is planned; there is no difference in the data.
- Each user only ever sees and changes their own plan (always the logged-in user, never from the request).
- `GET /meal-plan?from=yyyy-mm-dd&to=yyyy-mm-dd` (needs `recipe:read`): entries of that range, both ends inclusive, ordered by date and slot, as `MealPlanDtos.MealPlanEntryResponse` `{ date, slot, recipeId, recipeName }`. `from` after `to`, or more than 400 days (`MealPlanController.MAX_DAYS`) → 400.
- `PUT /meal-plan` `{ dates, slot, recipeId }` (`SetMealRequest`, needs `recipe:write`): sets the slot to the recipe on every date (several days at once), `recipeId: null` clears it. Returns all entries of those dates afterwards. No dates / more than 400 / no slot → 400. The recipe must be visible to the user (own or shared): unknown → 404, someone else's private recipe → 403.
- Tests: `MealPlanTests`.

## Settings
- Table `user_settings` (`UserSettings`, one row per user, `user_id` unique): `use_ai` plus two per-meal targets, `protein` (g, columns `protein_goal/min/max`) and `calories` (kcal, `kcal_goal/min/max`). `MealTarget` is either a `goal`, or a range of `min` and/or `max` (one side alone means "at least" / "at most").
- A user without a row gets the defaults (`useAi = true`, no targets, `SettingsDto.DEFAULTS`). GET never writes, the first PUT creates the row.
- `GET /settings` → `SettingsDtos.SettingsDto` `{ useAi, protein, calories }`, where a target is `{ goal, min, max }` or `null`. `PUT /settings` (same body) replaces everything and returns the saved settings. Any logged-in user, always their own settings.
- Validation (`SettingsController.validate`, 400): goal together with min/max → "Choose either a goal or a range"; a value ≤ 0 → "Targets must be positive"; min > max → "The minimum can't be above the maximum".
- AI gate: `RecipeService` passes `UserSettingsService.useAi(username)` to `NutritionService.suggest(…, useAi)`, which passes it to `IngredientNutritionCache.lookup(…, fetchMissing)`. With AI off, only cached ingredients count, Gemini is never called, and unknown macros stay null. The overloads without the flag mean "AI on" (used by the `Seeder`).
- Tests: `SettingsTests`.

## Nutrition (`com.homie.nutrition`)
- A recipe has `nutrition` (`Nutrition` embeddable, **per portion**): `{ fat, carbs, protein, calories, suggested }` in grams/kcal, or `null` if nothing could be calculated. Each macro may be null on its own.
- `calories` is always derived by `NutritionCalculator.calories` (9 kcal/g fat, 4 kcal/g carbs and protein, macros rounded to 0.1 g first). It is null unless all three macros are known, and is never accepted from a request.
- `NutritionService` is the entry point `RecipeService` uses:
  - `suggest(ingredients, portions)` → calculated values with `suggested = true`;
  - `fromUser(fat, carbs, protein)` → the user's values with `suggested = false`.
- Request: optional `nutrition: { fat, carbs, protein }` on `CreateRecipeRequest`. If present, all three are required (400 "Enter fat, carbs and protein") and none may be negative (400 "Macros can't be negative").
  - **create:** with `nutrition`, the user's values are stored; without it, values are suggested.
  - **update:** with `nutrition`, the user's values are stored. Without it, an unchanged suggestion is kept (same ingredients, amounts compared by value, and same portions). Otherwise values are suggested again. So the frontend sends the user's values back to keep them, and sends `null` to go back to a suggestion.
- How a suggestion is calculated (`NutritionCalculator.perPortion`):
  - Only ingredients with an amount count; "salt" without an amount is ignored.
  - Grams are the amount for `G`, × 1000 for `KG`, and otherwise × the cached weight of one unit. An amount without a unit counts as `PIECE`.
  - Grams × the macros per gram, summed and divided by the portions.
  - A macro is null if it is unknown for any counted ingredient.
- Cache (global, not per user) used by `IngredientNutritionCache.lookup`:
  - table `ingredient_nutrition`: macros **per gram** per ingredient name;
  - table `ingredient_unit_weight`: grams of one unit per (name, unit), e.g. potato `PIECE` = 170 g.
  - Names are normalized: trimmed, whitespace collapsed, lowercased.
  - Only missing entries are fetched, with **one** `NutritionSource.fetch` call per recipe, and the answers are stored. Nothing is stored for what the source didn't answer, so it is asked again next time.
  - Because of `create-drop`, the cache is emptied on every restart.
- `GeminiNutritionSource` (the `NutritionSource`):
  - Uses `gemini-3-flash-preview` with a JSON `responseSchema`. It asks for macros per 100 g and stores them divided by 100.
  - It drops answers to things it didn't ask for, and implausible values (negative, > 100 g per 100 g, weight ≤ 0).
  - Without `GOOGLE_API_KEY`, or on any error (logged as a warning), it answers nothing. The recipe is still saved, with the unknown macros null.
  - The call is synchronous, so creating a recipe with new ingredients takes a few seconds.
- The `Seeder` fills the cache for the ingredients of `user`'s three recipes and suggests their nutrition without Gemini. The other seeded recipes have no nutrition until they are edited.
- Tests mock `NutritionSource` with `@MockitoBean`, so they never call Gemini (see `NutritionTests`).

## Commands
- `./mvnw compile`
- `./mvnw test`
- `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (local dev with seed data)
