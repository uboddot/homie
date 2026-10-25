export type Recipe = {
    id: number;
    name: string;
    description: string;
    ingredients: string[];
    preparationTime: number; // in minutes
    ownerUsername: string | null;
    shared: boolean;
    steps: Step[];
}

/** How a recipe is displayed, see Components/Recipe/Recipe.tsx */
export type RecipeView = 'card' | 'list';

export type Step = {
    id: number;
    stepOrder: number;
    instruction: string;
}

/** Body of POST /recipes (backend: RecipeDtos.CreateRecipeRequest); the owner is set by the backend */
export type NewRecipe = {
    name: string;
    description: string;
    preparationTime: number | null; // in minutes
    ingredients: string[];
    steps: string[]; // instructions in order
    shared: boolean;
}
