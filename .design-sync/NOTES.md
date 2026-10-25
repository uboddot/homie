# design-sync notes (Homie)

## How this repo is synced
- Homie is an app, not a library, so there is no `dist/`. `.design-sync/make-pkg.mjs` (run by `cfg.buildCmd` from `frontend/`) generates a pseudo-installed package `frontend/node_modules/homie-ui`:
  - `index.ts`: barrel of the synced components (lists `UI` and `RECIPE` at the top of the script). To add or remove a component, edit those lists.
  - `types/`: `.d.ts` emitted by `tsc` from `src/`, with `@/` aliases rewritten to relative paths (the converter's type resolver ignores tsconfig `paths`; unresolved aliases silently drop props, e.g. ToggleGroup `variant`/`size`).
  - `styles.css`: copy of the single vite-built CSS (`dist/assets/index-*.css`: Tailwind utilities, theme tokens, all component `.css`). This is `cfg.cssEntry`.
- It lives in `node_modules` on purpose: types there resolve `class-variance-authority`/`radix-ui` from `frontend/node_modules`, and the folder is already gitignored and eslint-ignored. `npm ci` wipes it; re-run `cfg.buildCmd`.
- Converter commands (repo root): `--node-modules frontend/node_modules`, no `--entry` (package.json `module: index.ts` is the entry). Config paths (`srcDir`, `tsconfig`, `cssEntry`, `docsMap`) are relative to `frontend/node_modules/homie-ui`.
- Playwright: the machine cache has chromium-1228, so `.ds-sync` installs `playwright@1.61.0`. Latest playwright pins a newer build and fails to launch.

## Scope decisions (user, first sync 2026-10-25)
- Synced: shadcn ui primitives + presentational recipe components. Excluded because they need router, auth, or API: Navbar, ProtectedRoute, ResendVerification, RecipeFormDialog, RecipeDetailsDialog, pages.
- The 20 root components have authored previews. The 32 shadcn sub-parts (CardHeader, DialogContent, SelectItem, ...) stay on floor cards; they're demonstrated inside their parents' previews.
- shadcn components land in group `general` (the `ui` dir name is generic to the converter); recipe ones in `recipe`. `Recipe` itself is regrouped via the `docsMap` stub `.design-sync/docs/Recipe.md`.

## Gotchas
- Previews may only rely on Tailwind classes the app source already uses: the CSS is the app's purged build. Previews use components + inline layout glue, never new utility classes.
- `RecipeListItem` / `Recipe view="list"` render an `<li>`. A standalone preview must wrap it in `<ul className="recipe-collection recipe-collection--list">`, or it loses its border and looks broken.
- Icons (lucide-react) are bundled into previews but are not on `window.Homie`; adding them via `extraEntries` would ship the whole icon set.
- `RecipeCard` shows `src/assets/react.svg` as its thumbnail (placeholder in the app itself), inlined as a data URL.

## Known render warns
- `[FONT_MISSING] "Avenir"`: accepted. The body font stack is `system-ui, Avenir, Helvetica, Arial`; the app ships no webfont and renders in the system font by design.

## Re-sync risks
- `styles.css` is the purged vite build: a component variant that the app never uses can be missing from the CSS. Rebuild (`cfg.buildCmd`) whenever frontend source changes.
- `make-pkg.mjs` expects exactly one CSS file in `frontend/dist/assets`; a vite code-split change would break it loudly.
- The component list in `make-pkg.mjs` is hand-maintained; a new component in `src/Components` is not picked up automatically.
- The `Recipe` data shape is duplicated in `.design-sync/conventions.md` (from `src/model/Recipe.ts`). Update it when the model changes.
- Preview recipe data is inlined per preview file; model changes can make previews stale.
