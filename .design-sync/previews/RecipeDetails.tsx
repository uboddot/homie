import { RecipeDetails } from 'homie-ui';

const carbonara = {
  id: 1,
  name: 'Spaghetti Carbonara',
  description: 'Creamy Roman pasta with egg, pecorino and crispy guanciale.',
  preparationTime: 25,
  portions: 4,
  ownerUsername: 'anna',
  shared: true,
  ingredients: [
    { amount: 400, unit: 'G', name: 'spaghetti' },
    { amount: 150, unit: 'G', name: 'guanciale' },
    { amount: 4, unit: 'PIECE', name: 'egg yolks' },
    { amount: 80, unit: 'G', name: 'pecorino romano' },
    { amount: 1, unit: 'TSP', name: 'black pepper, freshly ground' },
    { amount: null, unit: null, name: 'salt' },
  ],
  steps: [
    { id: 1, stepOrder: 1, instruction: 'Boil the spaghetti in well-salted water until al dente.' },
    { id: 2, stepOrder: 2, instruction: 'Fry the guanciale in a dry pan until crisp.' },
    { id: 3, stepOrder: 3, instruction: 'Whisk the yolks with the pecorino and pepper.' },
    { id: 4, stepOrder: 4, instruction: 'Toss the pasta with the guanciale off the heat, then stir in the egg mixture with a splash of pasta water.' },
  ],
};

export const Default = () => (
  <div style={{ maxWidth: 520 }}>
    <RecipeDetails recipe={carbonara} />
  </div>
);

export const SinglePortionNoSteps = () => (
  <div style={{ maxWidth: 520 }}>
    <RecipeDetails
      recipe={{
        id: 2,
        name: 'Overnight Oats',
        description: 'Quick breakfast prepared the evening before.',
        preparationTime: 5,
        portions: 1,
        ownerUsername: null,
        shared: false,
        ingredients: [
          { amount: 50, unit: 'G', name: 'rolled oats' },
          { amount: 150, unit: 'ML', name: 'milk' },
          { amount: 1, unit: 'TBSP', name: 'honey' },
          { amount: 0.5, unit: 'PIECE', name: 'banana' },
        ],
        steps: [],
      }}
    />
  </div>
);
