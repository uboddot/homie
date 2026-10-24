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

export type Step = {
    id: number;
    stepOrder: number;
    instruction: string;
}
