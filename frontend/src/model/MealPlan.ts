/** Mirrors backend model.MealSlot */
export type MealSlot = 'BREAKFAST' | 'LUNCH' | 'DINNER';

/** The slots of a day, in the order they are shown. */
export const MEAL_SLOTS: { id: MealSlot; label: string }[] = [
    { id: 'BREAKFAST', label: 'Breakfast' },
    { id: 'LUNCH', label: 'Lunch' },
    { id: 'DINNER', label: 'Dinner' },
];

/** One planned meal (backend: MealPlanDtos.MealPlanEntryResponse) */
export type MealPlanEntry = {
    date: string; // yyyy-mm-dd
    slot: MealSlot;
    recipeId: number;
    recipeName: string;
}

/** Body of PUT /meal-plan (backend: MealPlanDtos.SetMealRequest): sets the slot on every date, recipeId null clears it */
export type SetMealInput = {
    dates: string[]; // yyyy-mm-dd
    slot: MealSlot;
    recipeId: number | null;
}
