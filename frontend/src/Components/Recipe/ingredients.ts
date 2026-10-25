import type { Ingredient, IngredientUnit } from '../../model/Recipe';
import { formatUnitAmount } from './units';

/** An ingredient while it's being typed: the amount stays text, so "0." isn't lost mid-input. */
export type IngredientRow = {
  amount: string;
  unit: IngredientUnit | null;
  name: string;
};

export const emptyIngredientRow: IngredientRow = { amount: '', unit: null, name: '' };

export function toIngredientRows(ingredients: Ingredient[]): IngredientRow[] {
  return ingredients.map(i => ({ amount: i.amount === null ? '' : String(i.amount), unit: i.unit, name: i.name }));
}

/** Converts the rows for the backend: trims names and drops completely empty rows. */
export function toIngredients(rows: IngredientRow[]): Ingredient[] {
  return rows
    .map(row => ({
      amount: row.amount.trim() === '' ? null : Number(row.amount),
      unit: row.unit,
      name: row.name.trim(),
    }))
    .filter(i => i.amount !== null || i.unit !== null || i.name !== '');
}

/** Same checks as the backend (RecipeController.validate), so the user sees them before saving. */
export function ingredientError(ingredients: Ingredient[]): string | null {
  if (ingredients.length === 0) {
    return 'Add at least one ingredient';
  }
  for (const i of ingredients) {
    if (i.amount !== null && !(i.amount > 0)) {
      return 'Amounts must be positive';
    }
    if (i.unit !== null && i.amount === null) {
      return 'A unit needs an amount';
    }
    if (i.name === '') {
      return 'Every amount needs an ingredient';
    }
  }
  return null;
}

/** "250", "1.5", "1 ½" (rounded for the unit, see units.ts), or "" without an amount. */
export function formatAmount(amount: number | null, unit: IngredientUnit | null): string {
  return amount === null ? '' : formatUnitAmount(amount, unit);
}
