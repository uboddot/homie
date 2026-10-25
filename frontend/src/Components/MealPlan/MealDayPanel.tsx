import { BookOpen, X } from 'lucide-react';
import { Badge } from '@/Components/ui/badge';
import { Button } from '@/Components/ui/button';
import { MEAL_SLOTS, type MealSlot } from '../../model/MealPlan';
import type { Recipe } from '../../model/Recipe';
import { dateKey, formatDay, formatLong, parseKey } from './dates';
import { type MealsByDate, slotValue } from './meals';
import RecipePicker from './RecipePicker';
import './MealDayPanel.css';

type MealDayPanelProps = {
  today: Date;
  days: string[]; // selected date keys, sorted
  meals: MealsByDate;
  recipes: Recipe[];
  saving: boolean;
  onPick: (slot: MealSlot, recipeId: number | null) => void;
  onShowRecipe: (recipe: Recipe) => void;
  onDone: () => void;
};

/** Side panel: today's meals while nothing is selected, otherwise a recipe picker per slot for the selected days. */
export default function MealDayPanel({ today, days, meals, recipes, saving, onPick, onShowRecipe, onDone }: MealDayPanelProps) {
  const todayKey = dateKey(today);

  if (days.length === 0) {
    const todayMeals = meals.get(todayKey) ?? {};
    return (
      <aside className="meal-day-panel">
        <div className="meal-day-panel-intro">
          <h2 className="meal-day-panel-title">Pick a day</h2>
          <p className="meal-day-panel-text">
            Select one or more days in the calendar to plan breakfast, lunch and dinner. Past days show what you ate.
          </p>
        </div>
        <div className="meal-day-panel-section">
          <span className="meal-day-panel-caption">Today</span>
          {MEAL_SLOTS.map(slot => (
            <div key={slot.id} className="meal-day-panel-today-row">
              <span className="meal-day-panel-today-label">{slot.label}</span>
              <span className="meal-day-panel-today-value">{todayMeals[slot.id]?.recipeName ?? '—'}</span>
            </div>
          ))}
        </div>
      </aside>
    );
  }

  const dates = days.map(parseKey);
  let title: string;
  let subtitle: string;
  if (days.length === 1) {
    title = formatLong(dates[0]);
    subtitle = days[0] === todayKey ? 'Today' : days[0] < todayKey ? 'What you ate' : 'What you’ll eat';
  } else {
    title = `${days.length} days selected`;
    subtitle = dates.slice(0, 6).map(formatDay).join(', ') + (days.length > 6 ? ` +${days.length - 6} more` : '');
  }
  const past = days.some(day => day < todayKey);
  const upcoming = days.some(day => day >= todayKey);
  const badge = past && upcoming ? 'Past & upcoming' : past ? 'Eaten' : 'Planned';

  return (
    <aside className="meal-day-panel">
      <div className="meal-day-panel-header">
        <div className="meal-day-panel-heading">
          <h2 className="meal-day-panel-title">{title}</h2>
          <p className="meal-day-panel-subtitle">{subtitle}</p>
        </div>
        <Badge variant="secondary">{badge}</Badge>
      </div>

      <div className="meal-day-panel-slots">
        {MEAL_SLOTS.map(slot => {
          const { entry, mixed } = slotValue(meals, days, slot.id);
          const recipe = entry ? recipes.find(r => r.id === entry.recipeId) : undefined;
          const hasValue = days.some(day => meals.get(day)?.[slot.id]);
          return (
            <div key={slot.id} className="meal-day-panel-slot">
              <div className="meal-day-panel-slot-head">
                <span className="meal-day-panel-slot-label">{slot.label}</span>
                <div className="meal-day-panel-slot-actions">
                  {recipe && (
                    <Button variant="ghost" size="xs" onClick={() => onShowRecipe(recipe)}>
                      <BookOpen /> Recipe
                    </Button>
                  )}
                  {hasValue && (
                    <Button variant="ghost" size="xs" disabled={saving} onClick={() => onPick(slot.id, null)}>
                      <X /> Clear
                    </Button>
                  )}
                </div>
              </div>
              <RecipePicker
                label={slot.label}
                recipes={recipes}
                recipeId={entry?.recipeId ?? null}
                recipeName={entry?.recipeName ?? null}
                mixed={mixed}
                disabled={saving}
                onPick={recipeId => onPick(slot.id, recipeId)}
              />
            </div>
          );
        })}
      </div>

      <div className="meal-day-panel-actions">
        <Button variant="ghost" size="sm" onClick={onDone}>Done</Button>
      </div>
    </aside>
  );
}
