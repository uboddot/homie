import { useState } from 'react';
import { Button } from '@/Components/ui/button';
import { UNIT_LABELS, type Recipe } from '../../model/Recipe';
import { formatAmount } from './ingredients';
import NutritionSummary from './NutritionSummary';
import PortionStepper from './PortionStepper';
import { scaleIngredient } from './units';
import './RecipeDetails.css';

/** Full information of a recipe: meta, ingredients (scalable to other portions, view only) and steps. */
export default function RecipeDetails({ recipe }: { recipe: Recipe }) {
  const [portions, setPortions] = useState(recipe.portions);
  const factor = portions / recipe.portions;
  const ingredients = recipe.ingredients.map(ingredient => scaleIngredient(ingredient, factor));
  const steps = [...(recipe.steps ?? [])].sort((a, b) => a.stepOrder - b.stepOrder);

  return (
    <div className="recipe-details">
      <p className="recipe-details-meta">
        {recipe.preparationTime} min · {recipe.portions} {recipe.portions === 1 ? 'portion' : 'portions'}
        {' '}· by {recipe.ownerUsername ?? 'unknown'} · {recipe.shared ? 'shared' : 'private'}
      </p>

      <section>
        <div className="recipe-details-ingredients-header">
          <h3 className="recipe-details-heading">Ingredients</h3>
          <div className="recipe-details-portions">
            {portions !== recipe.portions && (
              <Button type="button" variant="link" size="sm" onClick={() => setPortions(recipe.portions)}>
                Reset
              </Button>
            )}
            <PortionStepper value={portions} onChange={setPortions} />
          </div>
        </div>
        <table className="recipe-details-ingredients">
          <tbody>
            {ingredients.map((ingredient, index) => (
              <tr key={index}>
                <td className="recipe-details-amount">{formatAmount(ingredient.amount, ingredient.unit)}</td>
                <td className="recipe-details-unit">{ingredient.unit ? UNIT_LABELS[ingredient.unit] : ''}</td>
                <td className="recipe-details-ingredient">{ingredient.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <NutritionSummary nutrition={recipe.nutrition} />

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
