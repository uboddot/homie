import type { MealPlanEntry, MealSlot } from '../../model/MealPlan';

/** Planned meals per date key, each day by slot. */
export type MealsByDate = Map<string, Partial<Record<MealSlot, MealPlanEntry>>>;

export function groupByDate(entries: MealPlanEntry[]): MealsByDate {
  const byDate: MealsByDate = new Map();
  for (const entry of entries) {
    byDate.set(entry.date, { ...byDate.get(entry.date), [entry.slot]: entry });
  }
  return byDate;
}

/** What one slot holds on all the given days: the shared entry (or null if all are empty), or `mixed`. */
export type SlotValue = { entry: MealPlanEntry | null; mixed: boolean };

export function slotValue(meals: MealsByDate, days: string[], slot: MealSlot): SlotValue {
  const entries = days.map(day => meals.get(day)?.[slot] ?? null);
  const ids = new Set(entries.map(entry => entry?.recipeId ?? null));
  return ids.size === 1 ? { entry: entries[0], mixed: false } : { entry: null, mixed: true };
}
