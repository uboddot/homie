import { Input } from '@/Components/ui/input';
import { Label } from '@/Components/ui/label';
import { ToggleGroup, ToggleGroupItem } from '@/Components/ui/toggle-group';
import type { MealTargetRow, TargetMode } from './mealTarget';
import './MealTargetField.css';

type MealTargetFieldProps = {
  id: string;
  label: string;
  unit: string;
  value: MealTargetRow;
  onChange: (value: MealTargetRow) => void;
};

/** A per-meal target, entered either as one goal or as a min–max range (each side optional). */
export default function MealTargetField({ id, label, unit, value, onChange }: MealTargetFieldProps) {
  const number = (key: 'goal' | 'min' | 'max', placeholder: string, name: string) => (
    <Input id={`${id}-${key}`} aria-label={`${label} ${name}`} type="number" min={0} step="any" inputMode="decimal" placeholder={placeholder}
      value={value[key]} onChange={e => onChange({ ...value, [key]: e.target.value })} />
  );

  return (
    <fieldset className="meal-target-field">
      <div className="meal-target-field-header">
        <Label asChild>
          <legend>{label}</legend>
        </Label>
        <ToggleGroup
          type="single"
          variant="outline"
          size="sm"
          value={value.mode}
          // radix sends '' when the active item is clicked again; keep the current mode then
          onValueChange={mode => mode && onChange({ ...value, mode: mode as TargetMode })}
          aria-label={`${label} as`}
        >
          <ToggleGroupItem value="goal">Goal</ToggleGroupItem>
          <ToggleGroupItem value="range">Range</ToggleGroupItem>
        </ToggleGroup>
      </div>

      {value.mode === 'goal' ? (
        <div className="meal-target-field-inputs">
          {number('goal', 'Goal', 'goal')}
          <span className="meal-target-field-unit">{unit}</span>
        </div>
      ) : (
        <div className="meal-target-field-inputs">
          {number('min', 'Min', 'minimum')}
          <span aria-hidden="true">–</span>
          {number('max', 'Max', 'maximum')}
          <span className="meal-target-field-unit">{unit}</span>
        </div>
      )}

      <p className="meal-target-field-hint">
        {value.mode === 'goal'
          ? 'What you aim for per meal. Leave empty for no goal.'
          : 'Fill in only one side for "at least" or "at most".'}
      </p>
    </fieldset>
  );
}
