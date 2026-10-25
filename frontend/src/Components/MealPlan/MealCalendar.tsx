import type { MouseEvent } from 'react';
import { MEAL_SLOTS } from '../../model/MealPlan';
import { type Calendar, dateKey, formatShort, mondayIndex, parseKey } from './dates';
import type { MealsByDate } from './meals';
import './MealCalendar.css';

const ROW_LABELS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

type MealCalendarProps = {
  calendar: Calendar;
  today: Date;
  meals: MealsByDate;
  selected: string[];
  onDayClick: (key: string, event: MouseEvent) => void;
};

/** Heatmap of the planned meals: one column per week, one row per weekday, darker = more meals. */
export default function MealCalendar({ calendar, today, meals, selected, onDayClick }: MealCalendarProps) {
  const todayKey = dateKey(today);
  const todayRow = mondayIndex(today);
  const rangeStart = dateKey(calendar.rangeStart);
  const rangeEnd = dateKey(calendar.rangeEnd);

  return (
    <section className={`meal-calendar meal-calendar--${calendar.tier}`}>
      <div className="meal-calendar-scroll">
        <div className="meal-calendar-grid">
          <div className="meal-calendar-rows">
            {ROW_LABELS.map((label, row) => (
              <div
                key={label}
                className={`meal-calendar-row-label${row === todayRow ? ' is-today' : ''}${row % 2 === 1 ? ' is-minor' : ''}`}
              >
                {label}
              </div>
            ))}
          </div>
          {calendar.weeks.map(week => (
            <div key={week.days[0]} className={`meal-week${week.newMonth ? ' meal-week--new-month' : ''}`}>
              <div className="meal-week-head">
                <span className="meal-week-month">{week.monthLabel}</span>
                {week.hasToday && <span className="meal-week-today">Today</span>}
              </div>
              {week.days.map(key => {
                const dayMeals = meals.get(key) ?? {};
                const count = MEAL_SLOTS.filter(slot => dayMeals[slot.id]).length;
                const upcoming = key > todayKey;
                const classes = [
                  'meal-day',
                  `meal-day--level-${count}`,
                  key === todayKey && 'is-today',
                  upcoming && count === 0 && 'is-upcoming-empty',
                  selected.includes(key) && 'is-selected',
                  (key < rangeStart || key > rangeEnd) && 'is-outside',
                ].filter(Boolean).join(' ');
                const title = `${formatShort(parseKey(key))} — `
                  + MEAL_SLOTS.map(slot => `${slot.label}: ${dayMeals[slot.id]?.recipeName ?? '—'}`).join(' · ');

                return (
                  <button
                    key={key}
                    type="button"
                    className={classes}
                    title={title}
                    aria-label={title}
                    aria-pressed={selected.includes(key)}
                    onClick={event => onDayClick(key, event)}
                  >
                    <span className="meal-day-number">{parseKey(key).getDate()}</span>
                    <span className="meal-day-dots">
                      {MEAL_SLOTS.map(slot => (
                        <span key={slot.id} className={`meal-day-dot${dayMeals[slot.id] ? ' is-filled' : ''}`} />
                      ))}
                    </span>
                  </button>
                );
              })}
            </div>
          ))}
        </div>
      </div>

      <div className="meal-calendar-footer">
        <span>Click a day · Ctrl/⌘-click to add days · Shift-click for a range</span>
        <div className="meal-calendar-legend">
          <span className="meal-calendar-legend-scale">
            <span>Fewer meals</span>
            <span className="meal-calendar-swatch meal-day--level-0" />
            <span className="meal-calendar-swatch meal-day--level-1" />
            <span className="meal-calendar-swatch meal-day--level-2" />
            <span className="meal-calendar-swatch meal-day--level-3" />
            <span>All three</span>
          </span>
          <span className="meal-calendar-legend-scale">
            <span className="meal-calendar-swatch is-upcoming-empty" />
            <span>Upcoming</span>
          </span>
        </div>
      </div>
    </section>
  );
}
