import type { RecipeViewProps } from './types';
import { selectableProps } from './selectable';
import './RecipeListItem.css';

export default function RecipeListItem({ recipe, onSelect }: RecipeViewProps) {
  return (
    <li className="recipe-list-item" {...selectableProps(recipe, onSelect)}>
      <div className="recipe-list-item-main">
        <span className="recipe-list-item-name">{recipe.name}</span>
        <span className="recipe-list-item-description">{recipe.description}</span>
      </div>
      <div className="recipe-list-item-meta">
        <span>{recipe.ingredients.length} ingredients</span>
        <span>{recipe.preparationTime} min</span>
        <span>by {recipe.ownerUsername ?? 'unknown'}</span>
        {recipe.shared && <span className="recipe-list-item-shared">shared</span>}
      </div>
    </li>
  );
}
