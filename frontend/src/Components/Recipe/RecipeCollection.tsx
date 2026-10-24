import type { Recipe as RecipeModel, RecipeView } from '../../model/Recipe';
import Recipe from './Recipe';
import './RecipeCollection.css';

type RecipeCollectionProps = {
  recipes: RecipeModel[];
  view: RecipeView;
  onSelect?: (recipe: RecipeModel) => void;
};

/** Lays out several recipes with the wrapper that fits the view (grid for cards, <ul> for the list). */
export default function RecipeCollection({ recipes, view, onSelect }: RecipeCollectionProps) {
  const items = recipes.map(recipe =>
    <Recipe key={recipe.id} recipe={recipe} view={view} onSelect={onSelect} />
  );

  if (view === 'list') {
    return <ul className="recipe-collection recipe-collection--list">{items}</ul>;
  }
  return <div className="recipe-collection recipe-collection--card">{items}</div>;
}
