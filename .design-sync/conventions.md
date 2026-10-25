# Homie conventions (read first)

Homie is a recipe app: people keep their own recipes and share them. The library is a small set of shadcn/ui primitives (`Button`, `Card`, `Dialog`, `Input`, `Label`, `Select`, `Sheet`, `Switch`, `Textarea`, `Toggle`, `ToggleGroup` and their parts) plus recipe components built on them (`Recipe`, `RecipeCard`, `RecipeListItem`, `RecipeCollection`, `RecipeDetails`, `PortionStepper`, `RecipeViewToggle`, `IngredientList`, `EditableList`).

## Setup
- No provider or wrapper is needed. Link `styles.css` once; it carries the theme tokens, the base styles (system-ui font stack, `bg-background text-foreground` on `body`) and every component's CSS.
- Dark theme: put `class="dark"` on an ancestor (e.g. `<html>`). The same tokens switch values.
- Dialogs and sheets are controlled: `<Dialog open={...} onOpenChange={...}>` with a `DialogContent` (header, title, description, footer inside). `Select` needs `SelectTrigger` + `SelectValue` + `SelectContent` with `SelectItem`s.
- Icons are not part of `window.Homie`. The recipe components bring their own; for your own buttons use text labels or inline SVG.

## Styling idiom
The app styles its own layout with **semantic class names in plain CSS that read theme tokens via `var(--…)`**. Tailwind utilities exist only inside the shadcn primitives (shipped precompiled); don't write new utility classes, they aren't in the stylesheet.

Tokens (all defined in `styles.css`'s closure, light + `.dark`):
- Surfaces: `--background`, `--foreground`, `--card`, `--card-foreground`, `--popover`, `--popover-foreground`
- Actions: `--primary`, `--primary-foreground`, `--secondary`, `--secondary-foreground`, `--accent`, `--accent-foreground`, `--destructive`
- Quiet text and lines: `--muted`, `--muted-foreground`, `--border`, `--input`, `--ring`
- Shape: `--radius` (0.625rem)

Variants are props, not classes: `Button variant="default|secondary|outline|ghost|destructive|link"`, `size="xs|sm|default|lg|icon|icon-xs|icon-sm|icon-lg"`; `Toggle`/`ToggleGroup variant="default|outline"`.

## Recipe data
Recipe components take a `recipe` object of this shape:
```ts
type Recipe = {
  id: number; name: string; description: string;
  preparationTime: number;        // minutes
  portions: number;               // ingredient amounts are for this many
  ownerUsername: string | null; shared: boolean;
  ingredients: { amount: number | null; unit: Unit | null; name: string }[];
  steps: { id: number; stepOrder: number; instruction: string }[];
  // per portion, null if unknown; suggested = calculated by the backend, not yet edited by a user
  nutrition: { fat: number | null; carbs: number | null; protein: number | null; calories: number | null; suggested: boolean } | null;
};
type Unit = 'G'|'KG'|'ML'|'CL'|'DL'|'L'|'TSP'|'TBSP'|'CUP'|'PINCH'|'PIECE'|'CLOVE'|'SLICE'|'CAN'|'BUNCH';
```
- One recipe: `<Recipe recipe={r} view="card" | "list" onSelect={...} />`. Many: `<RecipeCollection recipes={rs} view="card" | "list" onSelect={...} />`. It renders the grid or the bordered list (`recipe-collection--list`). A list-view item is an `<li>`, so never render it outside a list.
- Detail view: `RecipeDetails` (meta line, scalable ingredient table with `PortionStepper`, nutrition per portion with a "Suggestion" badge, numbered steps), typically inside `DialogContent`.
- Forms: `IngredientList` takes rows `{ amount: string; unit: Unit | null; name: string }[]` plus `onChange`; `EditableList` takes `items: string[]`, `label`, `onChange`, and `ordered` for numbered steps. Both are controlled.

## Where the truth lives
`styles.css` → `_ds_bundle.css` (tokens + all component CSS), and each `components/<group>/<Name>/<Name>.prompt.md` / `.d.ts`.

## Example
```jsx
const { Button, Dialog, DialogContent, DialogHeader, DialogTitle, RecipeCollection, RecipeDetails, RecipeViewToggle } = window.Homie;

function RecipesScreen({ recipes }) {
  const [view, setView] = React.useState('card');
  const [selected, setSelected] = React.useState(null);
  return (
    <main className="recipes-page">
      <header className="recipes-header">
        <h1>My recipes</h1>
        <RecipeViewToggle view={view} onChange={setView} />
      </header>
      <RecipeCollection recipes={recipes} view={view} onSelect={setSelected} />
      <Button size="lg" className="recipes-new">New recipe</Button>
      <Dialog open={selected !== null} onOpenChange={open => !open && setSelected(null)}>
        <DialogContent>
          <DialogHeader><DialogTitle>{selected?.name}</DialogTitle></DialogHeader>
          {selected && <RecipeDetails recipe={selected} />}
        </DialogContent>
      </Dialog>
    </main>
  );
}
```
```css
.recipes-page { max-width: 960px; margin: 0 auto; padding: 1.5rem; }
.recipes-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.recipes-header h1 { color: var(--foreground); }
.recipes-new { position: fixed; bottom: 1.5rem; left: 50%; transform: translateX(-50%); }
```
