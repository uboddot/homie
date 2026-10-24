# Frontend

TypeScript + React 19 (function components and hooks), built with Vite.

## Styling: only in `.css` files
- All styles go in `.css` files, usually one per component next to it (`Card.tsx` + `Card.css`). Global styles go in `index.css`.
- No inline `style={{...}}`, no CSS-in-JS, and no Tailwind/utility classes in our own components. Use semantic class names.
- **Exception:** generated shadcn components in `src/Components/ui/` keep their Tailwind classes as generated. To restyle one, wrap it and style the wrapper via `.css`, or adjust theme variables in CSS.

## UI library: shadcn
- Use a shadcn component whenever one fits (Button, Card, Dialog, Input, Form, Select, Tabs, …) instead of building it by hand.
- Add components with `npx shadcn@latest add <component>`. They land in `src/Components/ui/`. Don't put our own components there.
- Build app-specific components (e.g. a recipe card) on top of the shadcn primitives in `src/Components/`.
- `components.json` maps the aliases to `@/Components` and `@/Components/ui` (capital C, it matters on Linux/Docker). `@` resolves to `src/` (in `vite.config.ts` and `tsconfig*.json`). `cn()` comes from `@/lib/utils`.
- Theme tokens (`--background`, `--primary`, `--muted-foreground`, …) are defined in `index.css`. Use them in our CSS via `var(--…)`.

## Structure (`src/`)
- `pages/`: route components (+ their `.css`)
- `Components/`: reusable components (+ their `.css`), e.g. `Header`, `RecipeCard`, `ProtectedRoute`
- `Components/ui/`: generated shadcn components
- `api/client.ts`: `apiFetch<T>()`, the only place that calls `fetch`
- `contexts/`: `AuthContext.tsx` (`AuthProvider`) + `auth.ts` (`useAuth()`)
- `model/`: TypeScript types that mirror backend DTOs/entities (`Recipe`, `User`)
- `lib/utils.ts`: shadcn `cn()` helper

## Routing & auth
- `react-router-dom`. Routes are in `App.tsx`. Pages that need a login go inside the `<ProtectedRoute />` route (it redirects to `/login`).
- The JWT is an HttpOnly cookie set by the backend, so JS never sees or stores it. `AuthProvider` restores the user on startup via `GET /auth/me`.
- Use `useAuth()` for `user`, `login`, `register` and `logout`.

## Backend API
- Always go through `apiFetch` (base URL `VITE_API_URL`, default `http://localhost:8080`, `credentials: 'include'`). It throws `ApiError` with `status`.
- Handle loading, error (401 = not logged in) and empty states separately.

## Commands
- `npm run dev`
- `npm run build` (type-check + build)
- `npm run lint`
