import { UNIT_LABELS, type Recipe } from '../../model/Recipe';
import { formatAmount } from './ingredients';
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
        <table className="recipe-details-ingredients">
          <tbody>
            {recipe.ingredients.map((ingredient, index) => (
              <tr key={index}>
                <td className="recipe-details-amount">{formatAmount(ingredient.amount)}</td>
                <td className="recipe-details-unit">{ingredient.unit ? UNIT_LABELS[ingredient.unit] : ''}</td>
                <td className="recipe-details-ingredient">{ingredient.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
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
