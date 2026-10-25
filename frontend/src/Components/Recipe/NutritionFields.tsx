import { Badge } from '@/Components/ui/badge';
import { Input } from '@/Components/ui/input';
import { Label } from '@/Components/ui/label';
import { calories, toNutritionInput, type NutritionRow } from './nutrition';
import './NutritionFields.css';

type NutritionFieldsProps = {
  value: NutritionRow;
  onChange: (value: NutritionRow) => void;
  /** Mark the values as a suggestion (calculated, not edited by the user yet). */
  suggested: boolean;
};

const FIELDS: { key: keyof NutritionRow; label: string }[] = [
  { key: 'fat', label: 'Fat (g)' },
  { key: 'carbs', label: 'Carbs (g)' },
  { key: 'protein', label: 'Protein (g)' },
];

/** Form field for the macros of one portion, with the derived calories as a preview. */
export default function NutritionFields({ value, onChange, suggested }: NutritionFieldsProps) {
  const input = toNutritionInput(value);

  return (
    <fieldset className="nutrition-fields">
      <Label asChild>
        <legend className="nutrition-fields-legend">
          Nutrition per portion
          {suggested && <Badge variant="secondary">Suggestion</Badge>}
        </legend>
      </Label>

      <div className="nutrition-fields-row">
        {FIELDS.map(({ key, label }) => (
          <div key={key} className="nutrition-fields-field">
            <Label htmlFor={`recipe-${key}`}>{label}</Label>
            <Input id={`recipe-${key}`} type="number" min={0} step={0.1} inputMode="decimal"
              value={value[key]} onChange={e => onChange({ ...value, [key]: e.target.value })} />
          </div>
        ))}
        <p className="nutrition-fields-calories" aria-live="polite">
          {input ? `≈ ${calories(input)} kcal` : '– kcal'}
        </p>
      </div>

      <p className="nutrition-fields-hint">
        {suggested
          ? 'Calculated from the ingredients. Change a value to set your own.'
          : 'Leave all empty to get a suggestion calculated from the ingredients.'}
      </p>
    </fieldset>
  );
}
