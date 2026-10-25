export type Recipe = {
    id: number;
    name: string;
    description: string;
    ingredients: Ingredient[];
    preparationTime: number; // in minutes
    portions: number; // the ingredient amounts are for this many portions
    ownerUsername: string | null;
    shared: boolean;
    steps: Step[];
}

/** How a recipe is displayed, see Components/Recipe/Recipe.tsx */
export type RecipeView = 'card' | 'list';

/** Mirrors backend model.IngredientUnit */
export type IngredientUnit =
    'G' | 'KG' | 'ML' | 'CL' | 'DL' | 'L' | 'TSP' | 'TBSP' | 'CUP' | 'PINCH' | 'PIECE' | 'CLOVE' | 'SLICE' | 'CAN' | 'BUNCH';

/** How each unit is shown; the key order is the order in the unit dropdown. */
export const UNIT_LABELS: Record<IngredientUnit, string> = {
    G: 'g',
    KG: 'kg',
    ML: 'ml',
    CL: 'cl',
    DL: 'dl',
    L: 'l',
    TSP: 'tsp',
    TBSP: 'tbsp',
    CUP: 'cup',
    PINCH: 'pinch',
    PIECE: 'pc',
    CLOVE: 'clove',
    SLICE: 'slice',
    CAN: 'can',
    BUNCH: 'bunch',
};

/** One row of the ingredient list, e.g. 250 g flour; amount and unit are optional ("salt") */
export type Ingredient = {
    amount: number | null;
    unit: IngredientUnit | null;
    name: string;
}

export type Step = {
    id: number;
    stepOrder: number;
    instruction: string;
}

/** Body of POST /recipes and PUT /recipes/{id} (backend: RecipeDtos.CreateRecipeRequest); the owner is set by the backend */
export type RecipeInput = {
    name: string;
    description: string;
    preparationTime: number | null; // in minutes
    portions: number | null; // the backend defaults to 4
    ingredients: Ingredient[];
    steps: string[]; // instructions in order
    shared: boolean;
}
