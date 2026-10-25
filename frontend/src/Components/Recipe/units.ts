import type { Ingredient, IngredientUnit } from '../../model/Recipe';

/** Units that can be converted into each other, small to large, with their size in the smallest one. */
type UnitFamily = {
  units: [IngredientUnit, number][];
  /** Only switch to a unit if the amount there is a whole quarter, so there is no "1.333 tbsp". */
  quarters: boolean;
};

const FAMILIES: UnitFamily[] = [
  { units: [['G', 1], ['KG', 1000]], quarters: false },
  { units: [['ML', 1], ['CL', 10], ['DL', 100], ['L', 1000]], quarters: false },
  { units: [['TSP', 1], ['TBSP', 3], ['CUP', 48]], quarters: true },
];

const EPSILON = 1e-9;

function isQuarter(value: number) {
  return Math.abs(value * 4 - Math.round(value * 4)) < EPSILON;
}

/**
 * The ingredient for `factor` times the portions, in a readable unit of the same family:
 * 1500 ml → 1.5 l, 0.25 kg → 250 g, 6 tsp → 2 tbsp. Units without a family (pieces, pinches, …)
 * are only multiplied. Factor 1 returns the ingredient as written.
 */
export function scaleIngredient(ingredient: Ingredient, factor: number): Ingredient {
  const { amount, unit } = ingredient;
  if (factor === 1 || amount === null) {
    return ingredient;
  }
  const family = FAMILIES.find(f => f.units.some(([u]) => u === unit));
  if (!family) {
    return { ...ingredient, amount: amount * factor };
  }

  const original = family.units.findIndex(([u]) => u === unit);
  const base = amount * family.units[original][1] * factor;
  // largest first; metric units stay in the written unit (or smaller) unless they reach the largest (1 kg / 1 l)
  const candidates = family.quarters
    ? [...family.units].reverse()
    : [family.units[family.units.length - 1], ...family.units.slice(0, original + 1).reverse()];
  const fits = ([, size]: [IngredientUnit, number]) => base / size >= 1 - EPSILON;
  const [bestUnit, bestSize] =
    candidates.find(c => fits(c) && (!family.quarters || isQuarter(base / c[1])))
    ?? candidates.find(fits)
    ?? family.units[0];
  return { ...ingredient, amount: base / bestSize, unit: bestUnit };
}
