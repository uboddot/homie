import type { MealTarget } from '../../model/Settings';

export type TargetMode = 'goal' | 'range';

/** A target while it's being typed: text, so "1." isn't lost mid-input; '' means not entered. */
export type MealTargetRow = {
  mode: TargetMode;
  goal: string;
  min: string;
  max: string;
};

export function toMealTargetRow(target: MealTarget | null): MealTargetRow {
  const text = (value: number | null | undefined) => (value == null ? '' : String(value));
  const isRange = target != null && target.goal == null && (target.min != null || target.max != null);
  return { mode: isRange ? 'range' : 'goal', goal: text(target?.goal), min: text(target?.min), max: text(target?.max) };
}

function parse(value: string): number | null {
  return value.trim() === '' ? null : Number(value);
}

/** The target for the backend; only the chosen mode's values are sent, null if nothing is entered. */
export function toMealTarget(row: MealTargetRow): MealTarget | null {
  const target = row.mode === 'goal'
    ? { goal: parse(row.goal), min: null, max: null }
    : { goal: null, min: parse(row.min), max: parse(row.max) };
  return target.goal === null && target.min === null && target.max === null ? null : target;
}

/** Same checks as the backend (SettingsController.validate). */
export function mealTargetError(row: MealTargetRow): string | null {
  const target = toMealTarget(row);
  if (target === null) {
    return null;
  }
  if ([target.goal, target.min, target.max].some(value => value !== null && !(value > 0))) {
    return 'Targets must be positive';
  }
  if (target.min !== null && target.max !== null && target.min > target.max) {
    return "The minimum can't be above the maximum";
  }
  return null;
}
