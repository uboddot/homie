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

/** How precisely an amount is shown: rounded to `step` (`smallStep` below 10), optionally as ¼ ½ ¾. */
type Precision = { step: number; smallStep?: number; fraction?: boolean };

const WEIGHT_OR_VOLUME: Precision = { step: 1, smallStep: 0.1 };
const ONE_DECIMAL: Precision = { step: 0.1 };
const SPOON: Precision = { step: 0.25, fraction: true };
const COUNT: Precision = { step: 0.5, fraction: true };

/** Display precision per unit; a full Record, so a new unit doesn't compile until it has a row. */
const PRECISION: Record<IngredientUnit, Precision> = {
  G: WEIGHT_OR_VOLUME,
  KG: ONE_DECIMAL,
  ML: WEIGHT_OR_VOLUME,
  CL: ONE_DECIMAL,
  DL: ONE_DECIMAL,
  L: ONE_DECIMAL,
  TSP: SPOON,
  TBSP: SPOON,
  CUP: SPOON,
  PINCH: { step: 1 },
  PIECE: COUNT,
  CLOVE: COUNT,
  SLICE: COUNT,
  CAN: COUNT,
  BUNCH: COUNT,
};

const FRACTIONS: Record<number, string> = { 0.25: '¼', 0.5: '½', 0.75: '¾' };

const EPSILON = 1e-9;

/**
 * The amount rounded for its unit (no unit counts like pieces), never down to 0:
 * 133.333 g → "133", 2.667 dl → "2.7", 1.333 tsp → "1 ¼", 0.1 pc → "½".
 */
export function formatUnitAmount(amount: number, unit: IngredientUnit | null): string {
  const precision = unit ? PRECISION[unit] : COUNT;
  const step = precision.smallStep !== undefined && amount < 10 ? precision.smallStep : precision.step;
  // EPSILON, because e.g. 1.25 / 0.1 is 12.4999… in floating point
  const rounded = Math.max(step, Math.round(amount / step + EPSILON) * step);
  if (!precision.fraction) {
    return new Intl.NumberFormat(undefined, { maximumFractionDigits: step < 1 ? 1 : 0 }).format(rounded);
  }
  const whole = Math.floor(rounded + EPSILON);
  const fraction = FRACTIONS[rounded - whole];
  if (!fraction) {
    return String(whole);
  }
  // thin space between whole number and fraction: "1 ½"
  return whole === 0 ? fraction : `${whole}\u2009${fraction}`;
}

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
