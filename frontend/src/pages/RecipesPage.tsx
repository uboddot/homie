import { useEffect, useState } from 'react';
import type { Recipe } from '../model/Recipe';
import RecipeCard from '../Components/RecipeCard';
import { apiFetch } from '../api/client';
import './RecipesPage.css';

export default function RecipesPage() {

  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiFetch<Recipe[]>('/recipes')
      .then(setRecipes)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [])

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
    <div className="page recipe-list">
      {recipes.map( recipe =>
        <RecipeCard key={recipe.id} {...recipe} />
      )}
    </div>
  )
}
