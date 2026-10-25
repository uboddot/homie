/**
 * A per-meal target (backend: SettingsDtos.MealTargetDto): either a goal, or a range where
 * min or max alone means "at least" / "at most".
 */
export type MealTarget = {
    goal: number | null;
    min: number | null;
    max: number | null;
}

/** GET /settings answer and PUT /settings body (backend: SettingsDtos.SettingsDto); null target = none */
export type Settings = {
    useAi: boolean;
    protein: MealTarget | null; // grams per meal
    calories: MealTarget | null; // kcal per meal
}
