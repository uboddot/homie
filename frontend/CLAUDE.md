# Frontend

TypeScript + React 19 (function components and hooks), built with Vite.

## Styling: only in `.css` files
- All styles go in `.css` files, usually one per component next to it (`Card.tsx` + `Card.css`). Global styles go in `index.css`.
- No inline `style={{...}}`, no CSS-in-JS, and no Tailwind/utility classes in our own components. Use semantic class names.
- **Exception:** generated shadcn components in `src/Components/ui/` keep their Tailwind classes as generated. To restyle one, wrap it and style the wrapper via `.css`, or adjust theme variables in CSS.

## UI library: shadcn
- Use a shadcn component whenever one fits (Button, Card, Dialog, Input, Form, Select, Tabs, …) instead of building it by hand.
- Add components with `npx shadcn@latest add <component>`. They land in `src/Components/ui/`. Don't put our own components there.
  - **After every `add`:** the CLI writes `import { cn } from "cn"` and installs an unrelated npm package `cn`. Fix it with `sed -i 's#from "cn"#from "@/lib/utils"#' src/Components/ui/*.tsx` and `npm uninstall cn`. The CLI also doesn't always install the icon library, so run `npm install lucide-react` if the build can't find it.
- Build app-specific components (e.g. a recipe card) on top of the shadcn primitives in `src/Components/`.
- `components.json` maps the aliases to `@/Components` and `@/Components/ui` (capital C, it matters on Linux/Docker). `@` resolves to `src/` (in `vite.config.ts` and `tsconfig*.json`). `cn()` comes from `@/lib/utils`.
- Theme tokens (`--background`, `--primary`, `--muted-foreground`, …) are defined in `index.css`. Use them in our CSS via `var(--…)`.

## Structure (`src/`)
- `pages/`: route components (+ their `.css`)
- `Components/`: reusable components (+ their `.css`), e.g. `Navbar` (links in its `navLinks` array, Sheet menu on mobile), `ProtectedRoute`, `LoadingBorder`
  - `LoadingBorder` wraps e.g. a button: `<LoadingBorder active={busy}><Button disabled={busy} …/></LoadingBorder>`. While active, an arc runs round its border (static ring with reduced motion). The ring takes no layout space, and a disabled shadcn button inside isn't faded.
- Toasts: shadcn `sonner` (`ui/sonner.tsx`), mounted once in `App.tsx` as `<Toaster position="bottom-right" theme="light" />`.
  - Show infos with `toast.success(...)` / `toast.info(...)` from `sonner`.
  - Form errors stay inline in their form.
- `Components/Recipe/`: everything that displays recipes
  - `Recipe.tsx` is the single entry point: `<Recipe recipe view="card" | "list" />`. It picks the display from its `views` map.
  - Views: `RecipeCard`, `RecipeListItem`. All take `RecipeViewProps` (`types.ts`: `recipe`, optional `onSelect`) and spread `selectableProps(recipe, onSelect)` (`selectable.ts`) on their root, so they are clickable and keyboard accessible.
  - Clicking a recipe opens `RecipeDetailsDialog` (shadcn Dialog). Its body is `RecipeDetails` (meta, ingredients, ordered steps), which can be reused e.g. on a detail page. A new view = a new component + an entry in `views` + an entry in the `RecipeView` type (`model/Recipe.ts`).
  - `RecipeCollection` lays out many recipes with the wrapper that fits the view. `RecipeViewToggle` switches the view.
  - `RecipeFormDialog` is the modal form to create (`POST /recipes`) or, with a `recipe` prop, edit (`PUT /recipes/{id}`) a recipe (body type `RecipeInput`). Create is opened by the fixed "New recipe" button at the bottom center of `RecipesPage`, which also shows when the list is empty. Edit is opened by the Edit button in `RecipeDetailsDialog`, which `RecipesPage` only shows for the recipe's owner (`onEdit` prop). `IngredientList` is its ingredients field (rows of amount, unit dropdown and ingredient). Form rows are `IngredientRow`s (amount kept as text); `ingredients.ts` converts them and mirrors the backend validation. `EditableList` is the add/remove-rows field for the (numbered) steps.
  - `RecipeDetails` shows ingredients as a table, so amounts (right-aligned), units and names line up. Unit labels come from `UNIT_LABELS` (`model/Recipe.ts`), which must list every backend `IngredientUnit`.
  - Scaling (view only, never saved): `RecipeDetails` has a `PortionStepper`; amounts are scaled by `chosen / recipe.portions` via `scaleIngredient` (`units.ts`), which also converts within a unit family (g/kg, ml/cl/dl/l, tsp/tbsp/cup, e.g. 1500 ml → 1.5 l). A unit not listed in `units.ts` is only multiplied, never converted. How precisely each unit's amount is shown (e.g. whole grams, 1 decimal for kg/l, ¼ steps for spoons as fractions, ½ for pieces) is the `PRECISION` table in `units.ts`, used by `formatAmount`. `RecipeDetailsDialog` keys `RecipeDetails` by id + portions, so the choice resets for another recipe or after an edit. The form's "Portions" field sets `recipe.portions`.
  - Nutrition (per portion, from the backend's `recipe.nutrition`, can be null):
    - `RecipeDetails` shows it in `NutritionSummary`, with "–" for an unknown value. It doesn't scale with the `PortionStepper`.
    - While `nutrition.suggested` is true, a "Suggestion" `Badge` is shown, both in `NutritionSummary` and in the form.
    - The form field is `NutritionFields` (fat, carbs and protein as text, plus a kcal preview from `calories()` in `nutrition.ts`, which mirrors the backend formula; the saved kcal always comes from the backend).
    - `RecipeFormDialog` sends `nutrition` only once the user has changed a value (`nutritionTouched`) or when the values were already the user's own. Otherwise it sends `null`, and the backend suggests values, or keeps the current suggestion if the ingredients are unchanged.
    - Emptying all three fields sends `null` too, which goes back to a suggestion.
    - Creating waits for the backend's Gemini lookup. Meanwhile the submit button reads "Extracting nutrition info..." inside a `LoadingBorder` (an edit reads "Saving...").
    - On success, a toast confirms it ("… created" / "… saved"). If a new recipe got no nutrition, the toast adds "Nutrition info couldn't be calculated."
  - Pages use `Recipe`/`RecipeCollection`, never a specific view directly.
- `Components/MealPlan/`: the meal plan (built from the Claude Design template "Meal planner")
  - `MealCalendar`: heatmap with one column per week (Mon–Sun rows); the color level is the number of planned slots. Cell size comes from a tier class (`meal-calendar--xl` … `--xs`, picked by `buildCalendar` from the number of weeks), state from classes (`is-today`, `is-selected`, `is-upcoming-empty`, `is-outside`), never inline styles.
  - `dates.ts`: date keys (`yyyy-mm-dd`, local time, compare as strings), formatting and `buildCalendar(today, back, ahead)` (whole weeks around the chosen range). `selection.ts`: `clickDay` (click / Ctrl-⌘ toggle / Shift range from the anchor). `meals.ts`: entries grouped per date, `slotValue` (shared value or "mixed" across selected days).
  - `MealDayPanel`: today's meals while nothing is selected, otherwise a `RecipePicker` per slot for all selected days, plus Clear and a "Recipe" button that opens `RecipeDetailsDialog`.
  - `RecipePicker`: combobox (shadcn `Popover` + `Command`/cmdk) that filters the visible recipes by name as you type. Items are keyed by id (names can repeat), so `Command` gets a custom `filter` on the name keyword. Esc inside it only closes the dropdown.
- `Components/Settings/`: `MealTargetField` (a per-meal target with a Goal | Range `ToggleGroup`, then one goal input or min–max inputs). `mealTarget.ts` keeps the values as text (`MealTargetRow`), converts them (only the chosen mode's values are sent, all empty → `null`) and mirrors the backend validation.
- `Components/ui/`: generated shadcn components
- `api/client.ts`: `apiFetch<T>()`, the only place that calls `fetch`
- `contexts/`: `AuthContext.tsx` (`AuthProvider`) + `auth.ts` (`useAuth()`)
- `model/`: TypeScript types that mirror backend DTOs/entities (`Recipe`, `User`, `MealPlan`, `Settings`)
- `lib/utils.ts`: shadcn `cn()` helper

## Routing & auth
- `react-router-dom`. Routes are in `App.tsx`. Pages that need a login go inside the `<ProtectedRoute />` route (it redirects to `/login`).
- Protected routes: `/` → `HomePage` (placeholder, links to recipes), `/recipes` → `RecipesPage`, `/meal-plan` → `MealPlanPage`, `/settings` → `SettingsPage`. Unknown paths redirect to `/`.
- The JWT is an HttpOnly cookie set by the backend, so JS never sees or stores it. `AuthProvider` restores the user on startup via `GET /auth/me`.
- Use `useAuth()` for `user`, `login`, `register`, `verifyEmail`, `resendVerification` and `logout`.
- Email confirmation: `register` doesn't log in. `RegisterPage` then shows "check your inbox". The mail links to `/verify-email?token=…` (`VerifyEmailPage`, public route), which calls `verifyEmail` once (guarded with a ref against StrictMode's double effect, because tokens are one-time) and then goes to `/`. Login with an unconfirmed email returns 403. `LoginPage` then shows `ResendVerification` (`Components/ResendVerification.tsx`).

## Meal plan page
- `MealPlanPage` loads `GET /recipes` once and `GET /meal-plan` for the whole calendar grid whenever the range changes. The "Look back" / "Plan ahead" choice is remembered in localStorage (`homie.mealPlanRange`).
- Picking or clearing calls `PUT /meal-plan` for all selected days and replaces those days' entries with the answer. Errors show as a toast. Esc clears the selection.

## Settings page
- `SettingsPage` loads `GET /settings` and saves everything with `PUT /settings`. It has a "Use AI features" `Switch` (off: the backend never asks Gemini for this user) and the per-meal protein (g) and calories (kcal) targets. Validation errors show inline, and success shows a toast.

## Backend API
- Always go through `apiFetch` (base URL `VITE_API_URL`, default `http://localhost:8080`, `credentials: 'include'`). It throws `ApiError` with `status`.
  - The prod image builds with `VITE_API_URL=/api`, and its nginx (`nginx/default.conf.template`) proxies `/api/*` to the backend (`BACKEND_URL`), so the browser sees one origin.
  - CSRF: on non-GET requests `apiFetch` sends the `XSRF-TOKEN` cookie back as the `X-XSRF-TOKEN` header. Without it the backend answers 403.
- Handle loading, error (401 = not logged in) and empty states separately.

## Commands
- `npm run dev`
- `npm run build` (type-check + build)
- Prod image: `docker build -t homie-frontend .` (multi-stage: Vite build, then nginx)
- `npm run lint`
