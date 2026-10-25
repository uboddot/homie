import { Badge } from '@/Components/ui/badge';
import type { Nutrition } from '../../model/Recipe';
import { formatGrams } from './nutrition';
import './NutritionSummary.css';

/** Calories and macros of one portion; "Suggestion" while they're calculated values nobody has edited yet. */
export default function NutritionSummary({ nutrition }: { nutrition: Nutrition | null }) {
  return (
    <section className="nutrition-summary">
      <div className="nutrition-summary-header">
        <h3 className="nutrition-summary-heading">Nutrition per portion</h3>
        {nutrition?.suggested && (
          <Badge variant="secondary" title="Calculated from the ingredients. Edit the recipe to correct it.">
            Suggestion
          </Badge>
        )}
      </div>
      {nutrition ? (
        <dl className="nutrition-summary-values">
          <div className="nutrition-summary-value">
            <dt>Calories</dt>
            <dd>{nutrition.calories === null ? '–' : `${nutrition.calories} kcal`}</dd>
          </div>
          <div className="nutrition-summary-value">
            <dt>Fat</dt>
            <dd>{formatGrams(nutrition.fat)}</dd>
          </div>
          <div className="nutrition-summary-value">
            <dt>Carbs</dt>
            <dd>{formatGrams(nutrition.carbs)}</dd>
          </div>
          <div className="nutrition-summary-value">
            <dt>Protein</dt>
            <dd>{formatGrams(nutrition.protein)}</dd>
          </div>
        </dl>
      ) : (
        <p className="nutrition-summary-empty">No nutrition info.</p>
      )}
    </section>
  );
}
