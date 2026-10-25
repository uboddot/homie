import type { Nutrition, NutritionInput } from '../../model/Recipe';

/** Macros while they're being typed: text, so "1." isn't lost mid-input; '' means not entered. */
export type NutritionRow = {
  fat: string;
  carbs: string;
  protein: string;
};

export const emptyNutritionRow: NutritionRow = { fat: '', carbs: '', protein: '' };

export function toNutritionRow(nutrition: Nutrition | null): NutritionRow {
  const text = (value: number | null) => (value === null ? '' : String(value));
  return nutrition
    ? { fat: text(nutrition.fat), carbs: text(nutrition.carbs), protein: text(nutrition.protein) }
    : emptyNutritionRow;
}

function parse(value: string): number | null {
  return value.trim() === '' ? null : Number(value);
}

/** The macros for the backend, or null if not all three are entered. */
export function toNutritionInput(row: NutritionRow): NutritionInput | null {
  const fat = parse(row.fat);
  const carbs = parse(row.carbs);
  const protein = parse(row.protein);
  return fat === null || carbs === null || protein === null ? null : { fat, carbs, protein };
}

export function isEmptyNutrition(row: NutritionRow): boolean {
  return [row.fat, row.carbs, row.protein].every(value => value.trim() === '');
}

/** Same checks as the backend (RecipeController.validate); all empty is fine, then the backend suggests values. */
export function nutritionError(row: NutritionRow): string | null {
  if (isEmptyNutrition(row)) {
    return null;
  }
  const values = [parse(row.fat), parse(row.carbs), parse(row.protein)];
  if (values.some(value => value === null)) {
    return 'Enter fat, carbs and protein, or leave all three empty';
  }
  if (values.some(value => !(value! >= 0))) {
    return "Macros can't be negative";
  }
  return null;
}

/**
 * Same formula as the backend (NutritionCalculator.calories): 9 kcal per g fat, 4 per g carbs and protein,
 * macros rounded to 0.1 g first. Only for the preview in the form; the saved value comes from the backend.
 */
export function calories(input: NutritionInput): number {
  const round = (value: number) => Math.round(value * 10) / 10;
  return Math.round(9 * round(input.fat) + 4 * round(input.carbs) + 4 * round(input.protein));
}

/** Grams with at most one decimal, "–" if unknown. */
export function formatGrams(value: number | null): string {
  return value === null ? '–' : `${value.toLocaleString(undefined, { maximumFractionDigits: 1 })} g`;
}
