export type DaySelection = {
  days: string[]; // sorted date keys
  anchor: string | null; // where a shift-click range starts
};

export const NO_SELECTION: DaySelection = { days: [], anchor: null };

type Modifiers = { shiftKey: boolean; ctrlKey: boolean; metaKey: boolean };

/**
 * Selection after clicking a day: a plain click selects only that day (or deselects it if it was the only one),
 * Ctrl/⌘ toggles it, Shift selects the range from the anchor (added to the selection with Ctrl/⌘ too).
 * `keys` are all days of the calendar in order.
 */
export function clickDay(selection: DaySelection, key: string, modifiers: Modifiers, keys: string[]): DaySelection {
  const additive = modifiers.ctrlKey || modifiers.metaKey;

  if (modifiers.shiftKey && selection.anchor) {
    const a = keys.indexOf(selection.anchor);
    const b = keys.indexOf(key);
    if (a >= 0 && b >= 0) {
      const range = keys.slice(Math.min(a, b), Math.max(a, b) + 1);
      const days = [...new Set([...(additive ? selection.days : []), ...range])].sort();
      return { days, anchor: selection.anchor };
    }
  }

  if (additive) {
    const days = selection.days.includes(key)
      ? selection.days.filter(day => day !== key)
      : [...selection.days, key].sort();
    return { days, anchor: key };
  }

  const onlyThis = selection.days.length === 1 && selection.days[0] === key;
  return { days: onlyThis ? [] : [key], anchor: key };
}
