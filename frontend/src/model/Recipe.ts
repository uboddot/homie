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
