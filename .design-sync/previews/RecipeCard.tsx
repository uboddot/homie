import { RecipeCard } from 'homie-ui';

const carbonara = {
  id: 1, name: 'Spaghetti Carbonara',
  description: 'Creamy Roman pasta with egg, pecorino and crispy guanciale.',
  preparationTime: 25, portions: 4, ownerUsername: 'anna', shared: true,
  ingredients: [{ amount: 400, unit: 'G', name: 'spaghetti' }], steps: [],
};

export const Clickable = () => <RecipeCard recipe={carbonara} onSelect={() => {}} />;

export const PrivateRecipe = () => (
  <RecipeCard
    recipe={{
      ...carbonara, id: 2, name: 'Red Lentil Soup',
      description: 'Warming soup with cumin, lemon and a swirl of yoghurt.',
      preparationTime: 40, ownerUsername: 'ben', shared: false,
    }}
  />
);
