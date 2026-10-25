---
category: recipe
---
Single entry point for showing one recipe: `<Recipe recipe={...} view="card" | "list" />` picks `RecipeCard` or `RecipeListItem`. Pass `onSelect` to make it clickable and keyboard accessible (e.g. to open `RecipeDetails` in a `Dialog`). To show many recipes use `RecipeCollection`, which wraps the items correctly for the view (a grid for cards, a `<ul>` for the list) - a bare `view="list"` item is an `<li>` and needs a list parent.
