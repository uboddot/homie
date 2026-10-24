import type { Recipe } from '../../model/Recipe';
import './RecipeDetails.css';

/** Full information of a recipe: meta, ingredients and steps. */
export default function RecipeDetails({ recipe }: { recipe: Recipe }) {
  const steps = [...(recipe.steps ?? [])].sort((a, b) => a.stepOrder - b.stepOrder);

  return (
    <div className="recipe-details">
      <p className="recipe-details-meta">
        {recipe.preparationTime} min · by {recipe.ownerUsername ?? 'unknown'} · {recipe.shared ? 'shared' : 'private'}
      </p>

      <section>
        <h3 className="recipe-details-heading">Ingredients</h3>
        <ul className="recipe-details-ingredients">
          {recipe.ingredients.map(ingredient => <li key={ingredient}>{ingredient}</li>)}
        </ul>
      </section>

      <section>
        <h3 className="recipe-details-heading">Steps</h3>
        {steps.length > 0 ? (
          <ol className="recipe-details-steps">
            {steps.map(step => <li key={step.id}>{step.instruction}</li>)}
          </ol>
        ) : (
          <p className="recipe-details-empty">No steps yet.</p>
        )}
      </section>
    </div>
  );
}
