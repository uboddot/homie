import { type MouseEvent, useEffect, useMemo, useState } from 'react';
import { toast } from 'sonner';
import { ToggleGroup, ToggleGroupItem } from '@/Components/ui/toggle-group';
import { apiFetch } from '../api/client';
import MealCalendar from '../Components/MealPlan/MealCalendar';
import MealDayPanel from '../Components/MealPlan/MealDayPanel';
import { buildCalendar, dateKey, formatShort, startOfToday } from '../Components/MealPlan/dates';
import { groupByDate } from '../Components/MealPlan/meals';
import { clickDay, type DaySelection, NO_SELECTION } from '../Components/MealPlan/selection';
import RecipeDetailsDialog from '../Components/Recipe/RecipeDetailsDialog';
import type { MealPlanEntry, MealSlot, SetMealInput } from '../model/MealPlan';
import type { Recipe } from '../model/Recipe';
import './MealPlanPage.css';

const BACK_OPTIONS: [number, string][] = [[0, 'None'], [28, '4 weeks'], [91, '3 months']];
const AHEAD_OPTIONS: [number, string][] = [[7, '1 week'], [14, '2 weeks'], [30, '1 month'], [91, '3 months']];
const RANGE_STORAGE_KEY = 'homie.mealPlanRange';

type Range = { back: number; ahead: number };

function readStoredRange(): Range {
  const fallback = { back: 0, ahead: 14 };
  try {
    const stored = JSON.parse(localStorage.getItem(RANGE_STORAGE_KEY) ?? 'null');
    const back = BACK_OPTIONS.some(([days]) => days === stored?.back) ? stored.back : fallback.back;
    const ahead = AHEAD_OPTIONS.some(([days]) => days === stored?.ahead) ? stored.ahead : fallback.ahead;
    return { back, ahead };
  } catch {
    return fallback;
  }
}

function storeRange(range: Range) {
  try {
    localStorage.setItem(RANGE_STORAGE_KEY, JSON.stringify(range));
  } catch {
    // storage unavailable (private mode etc.), the choice just isn't remembered
  }
}

type RangeToggleProps = {
  label: string;
  options: [number, string][];
  value: number;
  onChange: (days: number) => void;
};

function RangeToggle({ label, options, value, onChange }: RangeToggleProps) {
  return (
    <div className="meal-plan-range">
      <span className="meal-plan-range-label">{label}</span>
      <ToggleGroup
        type="single"
        variant="outline"
        size="sm"
        value={String(value)}
        // radix sends '' when the active item is clicked again; keep the current value then
        onValueChange={days => days && onChange(Number(days))}
        aria-label={label}
      >
        {options.map(([days, text]) => (
          <ToggleGroupItem key={days} value={String(days)}>{text}</ToggleGroupItem>
        ))}
      </ToggleGroup>
    </div>
  );
}

export default function MealPlanPage() {
  const [today] = useState(startOfToday);
  const [range, setRange] = useState<Range>(readStoredRange);
  const [entries, setEntries] = useState<MealPlanEntry[] | null>(null);
  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [selection, setSelection] = useState<DaySelection>(NO_SELECTION);
  const [saving, setSaving] = useState(false);
  const [shownRecipe, setShownRecipe] = useState<Recipe | null>(null);

  const calendar = useMemo(() => buildCalendar(today, range.back, range.ahead), [today, range]);
  const meals = useMemo(() => groupByDate(entries ?? []), [entries]);

  useEffect(() => {
    apiFetch<Recipe[]>('/recipes')
      .then(list => setRecipes([...list].sort((a, b) => a.name.localeCompare(b.name))))
      .catch(err => setError(err.message));
  }, []);

  // the whole grid, so the faded days around the range show their meals too
  useEffect(() => {
    let current = true;
    apiFetch<MealPlanEntry[]>(`/meal-plan?from=${calendar.first}&to=${calendar.last}`)
      .then(list => current && setEntries(list))
      .catch(err => current && setError(err.message));
    return () => {
      current = false;
    };
  }, [calendar.first, calendar.last]);

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      // an open recipe dialog closes first
      if (event.key === 'Escape' && !shownRecipe) setSelection(NO_SELECTION);
    }
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [shownRecipe]);

  function changeRange(change: Partial<Range>) {
    const next = { ...range, ...change };
    setRange(next);
    storeRange(next);
  }

  function handleDayClick(key: string, event: MouseEvent) {
    setSelection(current => clickDay(current, key, event, calendar.keys));
  }

  async function setMeal(slot: MealSlot, recipeId: number | null) {
    const body: SetMealInput = { dates: selection.days, slot, recipeId };
    setSaving(true);
    try {
      // the answer has every slot of the changed days
      const changed = await apiFetch<MealPlanEntry[]>('/meal-plan', { method: 'PUT', body: JSON.stringify(body) });
      setEntries(prev => [...(prev ?? []).filter(entry => !body.dates.includes(entry.date)), ...changed]);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Couldn’t save the meal');
    } finally {
      setSaving(false);
    }
  }

  if (error) {
    return <div className="page-message">{error}</div>;
  }

  if (entries === null) {
    return <div className="page-message">Loading...</div>;
  }

  const todayKey = dateKey(today);
  const rangeEndKey = dateKey(calendar.rangeEnd);
  const planned = entries.filter(entry => entry.date >= todayKey && entry.date <= rangeEndKey).length;

  return (
    <div className="page meal-plan-page">
      <header className="meal-plan-header">
        <div className="meal-plan-heading">
          <h1 className="meal-plan-title">Meal plan</h1>
          <p className="meal-plan-summary">
            {formatShort(calendar.rangeStart)} – {formatShort(calendar.rangeEnd)} · {planned} meals planned
          </p>
        </div>
        <div className="meal-plan-ranges">
          <RangeToggle label="Look back" options={BACK_OPTIONS} value={range.back} onChange={back => changeRange({ back })} />
          <RangeToggle label="Plan ahead" options={AHEAD_OPTIONS} value={range.ahead} onChange={ahead => changeRange({ ahead })} />
        </div>
      </header>

      <div className="meal-plan-body">
        <MealCalendar calendar={calendar} today={today} meals={meals} selected={selection.days} onDayClick={handleDayClick} />
        <MealDayPanel
          today={today}
          days={selection.days}
          meals={meals}
          recipes={recipes}
          saving={saving}
          onPick={setMeal}
          onShowRecipe={setShownRecipe}
          onDone={() => setSelection(NO_SELECTION)}
        />
      </div>

      <RecipeDetailsDialog recipe={shownRecipe} onClose={() => setShownRecipe(null)} />
    </div>
  );
}
