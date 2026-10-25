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
- `Components/`: reusable components (+ their `.css`), e.g. `Navbar` (links in its `navLinks` array, Sheet menu on mobile), `ProtectedRoute`
- `Components/Recipe/`: everything that displays recipes
  - `Recipe.tsx` is the single entry point: `<Recipe recipe view="card" | "list" />`. It picks the display from its `views` map.
  - Views: `RecipeCard`, `RecipeListItem`. All take `RecipeViewProps` (`types.ts`: `recipe`, optional `onSelect`) and spread `selectableProps(recipe, onSelect)` (`selectable.ts`) on their root, so they are clickable and keyboard accessible.
  - Clicking a recipe opens `RecipeDetailsDialog` (shadcn Dialog). Its body is `RecipeDetails` (meta, ingredients, ordered steps), which can be reused e.g. on a detail page. A new view = a new component + an entry in `views` + an entry in the `RecipeView` type (`model/Recipe.ts`).
  - `RecipeCollection` lays out many recipes with the wrapper that fits the view. `RecipeViewToggle` switches the view.
  - `RecipeFormDialog` is the modal form to create (`POST /recipes`) or, with a `recipe` prop, edit (`PUT /recipes/{id}`) a recipe (body type `RecipeInput`). Create is opened by the fixed "New recipe" button at the bottom center of `RecipesPage`, which also shows when the list is empty. Edit is opened by the Edit button in `RecipeDetailsDialog`, which `RecipesPage` only shows for the recipe's owner (`onEdit` prop). `IngredientList` is its ingredients field (rows of amount, unit dropdown and ingredient). Form rows are `IngredientRow`s (amount kept as text); `ingredients.ts` converts them and mirrors the backend validation. `EditableList` is the add/remove-rows field for the (numbered) steps.
  - `RecipeDetails` shows ingredients as a table, so amounts (right-aligned), units and names line up. Unit labels come from `UNIT_LABELS` (`model/Recipe.ts`), which must list every backend `IngredientUnit`.
  - Scaling (view only, never saved): `RecipeDetails` has a `PortionStepper`; amounts are scaled by `chosen / recipe.portions` via `scaleIngredient` (`units.ts`), which also converts within a unit family (g/kg, ml/cl/dl/l, tsp/tbsp/cup, e.g. 1500 ml → 1.5 l). A unit not listed in `units.ts` is only multiplied, never converted. `RecipeDetailsDialog` keys `RecipeDetails` by id + portions, so the choice resets for another recipe or after an edit. The form's "Portions" field sets `recipe.portions`.
  - Pages use `Recipe`/`RecipeCollection`, never a specific view directly.
- `Components/ui/`: generated shadcn components
- `api/client.ts`: `apiFetch<T>()`, the only place that calls `fetch`
- `contexts/`: `AuthContext.tsx` (`AuthProvider`) + `auth.ts` (`useAuth()`)
- `model/`: TypeScript types that mirror backend DTOs/entities (`Recipe`, `User`)
- `lib/utils.ts`: shadcn `cn()` helper

## Routing & auth
- `react-router-dom`. Routes are in `App.tsx`. Pages that need a login go inside the `<ProtectedRoute />` route (it redirects to `/login`).
- Protected routes: `/` → `HomePage` (placeholder, links to recipes), `/recipes` → `RecipesPage`. Unknown paths redirect to `/`.
- The JWT is an HttpOnly cookie set by the backend, so JS never sees or stores it. `AuthProvider` restores the user on startup via `GET /auth/me`.
- Use `useAuth()` for `user`, `login`, `register`, `verifyEmail`, `resendVerification` and `logout`.
- Email confirmation: `register` doesn't log in. `RegisterPage` then shows "check your inbox". The mail links to `/verify-email?token=…` (`VerifyEmailPage`, public route), which calls `verifyEmail` once (guarded with a ref against StrictMode's double effect, because tokens are one-time) and then goes to `/`. Login with an unconfirmed email returns 403. `LoginPage` then shows `ResendVerification` (`Components/ResendVerification.tsx`).

## Backend API
- Always go through `apiFetch` (base URL `VITE_API_URL`, default `http://localhost:8080`, `credentials: 'include'`). It throws `ApiError` with `status`.
- Handle loading, error (401 = not logged in) and empty states separately.

## Commands
- `npm run dev`
- `npm run build` (type-check + build)
- `npm run lint`
