const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const MONTHS_LONG = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October',
  'November', 'December'];
const DAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const DAYS_LONG = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

/** Local date as yyyy-mm-dd, the format the backend uses. These keys sort chronologically as strings. */
export function dateKey(date: Date): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

export function parseKey(key: string): Date {
  const [year, month, day] = key.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

export function startOfToday(): Date {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return today;
}

/** 0 = Monday … 6 = Sunday */
export function mondayIndex(date: Date): number {
  return (date.getDay() + 6) % 7;
}

/** e.g. "Tue Oct 27" */
export function formatShort(date: Date): string {
  return `${DAYS[date.getDay()]} ${MONTHS[date.getMonth()]} ${date.getDate()}`;
}

/** e.g. "Tuesday, October 27" */
export function formatLong(date: Date): string {
  return `${DAYS_LONG[date.getDay()]}, ${MONTHS_LONG[date.getMonth()]} ${date.getDate()}`;
}

/** e.g. "Oct 27" */
export function formatDay(date: Date): string {
  return `${MONTHS[date.getMonth()]} ${date.getDate()}`;
}

/** How large the day cells are; the more weeks are shown, the smaller (see MealCalendar.css). */
export type CalendarTier = 'xl' | 'lg' | 'md' | 'sm' | 'xs';

function tierFor(weeks: number): CalendarTier {
  if (weeks <= 3) return 'xl';
  if (weeks <= 6) return 'lg';
  if (weeks <= 10) return 'md';
  if (weeks <= 18) return 'sm';
  return 'xs';
}

export type CalendarWeek = {
  days: string[]; // 7 date keys, Monday first
  monthLabel: string; // set on the week where a month starts (and on the first week)
  newMonth: boolean; // a month starts in this week (not the first one), gets a little extra space
  hasToday: boolean;
};

export type Calendar = {
  rangeStart: Date; // first day the user asked for
  rangeEnd: Date; // last day the user asked for
  first: string; // first key of the grid (a Monday, can be before rangeStart)
  last: string; // last key of the grid (a Sunday)
  keys: string[]; // every key of the grid in order, for shift-click ranges
  weeks: CalendarWeek[];
  tier: CalendarTier;
};

/** Whole weeks (Monday to Sunday) from `back` days before today to `ahead - 1` days after it. */
export function buildCalendar(today: Date, back: number, ahead: number): Calendar {
  const rangeStart = addDays(today, -back);
  const rangeEnd = addDays(today, ahead - 1);
  const start = addDays(rangeStart, -mondayIndex(rangeStart));
  const end = addDays(rangeEnd, 6 - mondayIndex(rangeEnd));
  const weekCount = Math.round((end.getTime() - start.getTime()) / 864e5 + 1) / 7;
  const tier = tierFor(weekCount);
  const longMonths = tier !== 'xs';
  const todayKey = dateKey(today);

  const keys: string[] = [];
  const weeks: CalendarWeek[] = [];
  for (let column = 0; column < weekCount; column++) {
    const days: string[] = [];
    let monthLabel = '';
    let newMonth = false;
    for (let row = 0; row < 7; row++) {
      const date = addDays(start, column * 7 + row);
      days.push(dateKey(date));
      if (date.getDate() === 1) {
        monthLabel = longMonths ? MONTHS_LONG[date.getMonth()] : MONTHS[date.getMonth()];
        newMonth = column > 0;
      }
    }
    // label the first week too, unless a month starts so soon that the labels would overlap
    if (column === 0 && !monthLabel) {
      const soon = longMonths ? 7 : 14;
      let nextSoon = false;
      for (let i = 1; i < soon && i < weekCount * 7; i++) {
        if (addDays(start, i).getDate() === 1) nextSoon = true;
      }
      if (!nextSoon) monthLabel = longMonths ? MONTHS_LONG[start.getMonth()] : MONTHS[start.getMonth()];
    }
    keys.push(...days);
    weeks.push({ days, monthLabel, newMonth, hasToday: days.includes(todayKey) });
  }

  return { rangeStart, rangeEnd, first: keys[0], last: keys[keys.length - 1], keys, weeks, tier };
}
