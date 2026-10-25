import { RecipeListItem } from 'homie-ui';

const base = {
  id: 1, name: 'Spaghetti Carbonara',
  description: 'Creamy Roman pasta with egg, pecorino and crispy guanciale.',
  preparationTime: 25, portions: 4, ownerUsername: 'anna', shared: true,
  ingredients: [
    { amount: 400, unit: 'G', name: 'spaghetti' },
    { amount: 150, unit: 'G', name: 'guanciale' },
    { amount: 4, unit: 'PIECE', name: 'egg yolks' },
    { amount: 80, unit: 'G', name: 'pecorino romano' },
  ],
  steps: [],
};

// A list item is an <li>: render it inside RecipeCollection, or a <ul> with its list classes as here.
export const Shared = () => (
  <ul className="recipe-collection recipe-collection--list" style={{ width: 560 }}>
    <RecipeListItem recipe={base} onSelect={() => {}} />
  </ul>
);

export const Private = () => (
  <ul className="recipe-collection recipe-collection--list" style={{ width: 560 }}>
    <RecipeListItem
      recipe={{ ...base, id: 2, name: 'Red Lentil Soup', description: 'Warming soup with cumin and lemon.', preparationTime: 40, ownerUsername: 'ben', shared: false }}
      onSelect={() => {}}
    />
  </ul>
);
