import { useEffect, useState } from 'react';
import type { Recipe, RecipeView } from '../model/Recipe';
import RecipeCollection from '../Components/Recipe/RecipeCollection';
import RecipeViewToggle from '../Components/Recipe/RecipeViewToggle';
import RecipeDetailsDialog from '../Components/Recipe/RecipeDetailsDialog';
import RecipeFormDialog from '../Components/Recipe/RecipeFormDialog';
import { Button } from '@/Components/ui/button';
import { Plus } from 'lucide-react';
import { apiFetch } from '../api/client';
import { useAuth } from '../contexts/auth';
import './RecipesPage.css';

const VIEW_STORAGE_KEY = 'homie.recipeView';

function readStoredView(): RecipeView {
  try {
    return localStorage.getItem(VIEW_STORAGE_KEY) === 'list' ? 'list' : 'card';
  } catch {
    return 'card';
  }
}

function storeView(view: RecipeView) {
  try {
    localStorage.setItem(VIEW_STORAGE_KEY, view);
  } catch {
    // storage unavailable (private mode etc.), the choice just isn't remembered
  }
}

export default function RecipesPage() {
  const { user } = useAuth();

  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<RecipeView>(readStoredView);
  const [selected, setSelected] = useState<Recipe | null>(null);
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<Recipe | null>(null);

  useEffect(() => {
    apiFetch<Recipe[]>('/recipes')
      .then(setRecipes)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [])

  function startEditing(recipe: Recipe) {
    setSelected(null);
    setEditing(recipe);
  }

  function changeView(newView: RecipeView) {
    setView(newView);
    storeView(newView);
  }

  if(loading) {
    return <div className="page-message">Loading...</div>;
  }

  if(error) {
    return <div className="page-message">{error}</div>;
  }

  return (
    <div className="page recipes-page">
      {recipes.length < 1 ? (
        // no early return: new users start empty and still need the create button
        <div className="page-message">No recipes yet. Create your first one!</div>
      ) : (
        <>
          <div className="recipes-toolbar">
            <RecipeViewToggle view={view} onChange={changeView} />
          </div>
          <RecipeCollection recipes={recipes} view={view} onSelect={setSelected} />
        </>
      )}
      <RecipeDetailsDialog
        recipe={selected}
        onClose={() => setSelected(null)}
        onEdit={selected && selected.ownerUsername === user?.username ? startEditing : undefined}
      />

      <Button size="lg" className="recipes-create-button" onClick={() => setCreating(true)}>
        <Plus /> New recipe
      </Button>
      <RecipeFormDialog
        open={creating}
        onOpenChange={setCreating}
        onSaved={recipe => setRecipes(prev => [recipe, ...prev])}
      />
      <RecipeFormDialog
        open={editing !== null}
        onOpenChange={open => !open && setEditing(null)}
        recipe={editing}
        onSaved={recipe => setRecipes(prev => prev.map(r => r.id === recipe.id ? recipe : r))}
      />
    </div>
  )
}
