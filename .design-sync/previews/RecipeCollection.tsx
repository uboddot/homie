import { RecipeCollection } from 'homie-ui';

const recipe = (id: number, name: string, description: string, preparationTime: number, ownerUsername: string, shared: boolean) => ({
  id, name, description, preparationTime, portions: 4, ownerUsername, shared,
  ingredients: [
    { amount: 400, unit: 'G', name: 'main ingredient' },
    { amount: 1, unit: 'TBSP', name: 'olive oil' },
    { amount: null, unit: null, name: 'salt' },
  ],
  steps: [],
});

const recipes = [
  recipe(1, 'Spaghetti Carbonara', 'Creamy Roman pasta with egg, pecorino and guanciale.', 25, 'anna', true),
  recipe(2, 'Red Lentil Soup', 'Warming soup with cumin, lemon and yoghurt.', 40, 'ben', false),
  recipe(3, 'Chicken Curry', 'Mild coconut curry with spinach and rice.', 45, 'anna', true),
];

export const Cards = () => <RecipeCollection recipes={recipes} view="card" onSelect={() => {}} />;

export const List = () => <RecipeCollection recipes={recipes} view="list" onSelect={() => {}} />;
