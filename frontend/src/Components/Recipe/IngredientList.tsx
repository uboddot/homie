import { Plus, X } from 'lucide-react';
import { Button } from '@/Components/ui/button';
import { Input } from '@/Components/ui/input';
import { Label } from '@/Components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/Components/ui/select';
import { UNIT_LABELS, type IngredientUnit } from '../../model/Recipe';
import { emptyIngredientRow, type IngredientRow } from './ingredients';
import './IngredientList.css';

// Radix Select doesn't allow an empty value, so "no unit" gets its own
const NO_UNIT = 'NONE';
const UNITS = Object.keys(UNIT_LABELS) as IngredientUnit[];

type IngredientListProps = {
  rows: IngredientRow[];
  onChange: (rows: IngredientRow[]) => void;
};

/** Form field for the ingredients: one row of amount, unit and ingredient each; rows can be added and removed. */
export default function IngredientList({ rows, onChange }: IngredientListProps) {
  function update(index: number, change: Partial<IngredientRow>) {
    onChange(rows.map((row, i) => (i === index ? { ...row, ...change } : row)));
  }

  function remove(index: number) {
    onChange(rows.filter((_, i) => i !== index));
  }

  return (
    <fieldset className="ingredient-list">
      <Label asChild><legend>Ingredients</legend></Label>
      {rows.length > 0 && (
        <div className="ingredient-list-row ingredient-list-header" aria-hidden="true">
          <span>Amount</span>
          <span>Unit</span>
          <span>Ingredient</span>
        </div>
      )}
      {rows.map((row, index) => (
        // index keys are fine: rows are fully controlled and have no own state
        <div className="ingredient-list-row" key={index}>
          <Input
            type="number"
            min={0}
            step="any"
            inputMode="decimal"
            aria-label={`Amount ${index + 1}`}
            placeholder="250"
            value={row.amount}
            onChange={e => update(index, { amount: e.target.value })}
          />
          <Select
            value={row.unit ?? NO_UNIT}
            onValueChange={value => update(index, { unit: value === NO_UNIT ? null : value as IngredientUnit })}
          >
            <SelectTrigger className="ingredient-list-unit" aria-label={`Unit ${index + 1}`}>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={NO_UNIT}>–</SelectItem>
              {UNITS.map(unit => <SelectItem key={unit} value={unit}>{UNIT_LABELS[unit]}</SelectItem>)}
            </SelectContent>
          </Select>
          <Input
            id={`recipe-ingredient-${index}`}
            aria-label={`Ingredient ${index + 1}`}
            placeholder="flour"
            value={row.name}
            onChange={e => update(index, { name: e.target.value })}
          />
          <Button type="button" variant="ghost" size="icon-sm" aria-label={`Remove ingredient ${index + 1}`}
            onClick={() => remove(index)}>
            <X />
          </Button>
        </div>
      ))}
      <Button type="button" variant="outline" size="sm" className="ingredient-list-add"
        onClick={() => onChange([...rows, emptyIngredientRow])}>
        <Plus /> Add ingredient
      </Button>
    </fieldset>
  );
}
