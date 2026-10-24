import { useEffect, useState } from 'react';
import type { Recipe, RecipeView } from '../model/Recipe';
import RecipeCollection from '../Components/Recipe/RecipeCollection';
import RecipeViewToggle from '../Components/Recipe/RecipeViewToggle';
import RecipeDetailsDialog from '../Components/Recipe/RecipeDetailsDialog';
import { apiFetch } from '../api/client';
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

  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<RecipeView>(readStoredView);
  const [selected, setSelected] = useState<Recipe | null>(null);

  useEffect(() => {
    apiFetch<Recipe[]>('/recipes')
      .then(setRecipes)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [])

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

  if(recipes.length < 1) {
    return <div className="page-message">No recipes found.</div>;
  }

  return (
    <div className="page">
      <div className="recipes-toolbar">
        <RecipeViewToggle view={view} onChange={changeView} />
      </div>
      <RecipeCollection recipes={recipes} view={view} onSelect={setSelected} />
      <RecipeDetailsDialog recipe={selected} onClose={() => setSelected(null)} />
    </div>
  )
}
