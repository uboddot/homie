import { Recipe } from 'homie-ui';

const curry = {
  id: 3, name: 'Chicken Curry',
  description: 'Mild coconut curry with chicken thighs, spinach and rice.',
  preparationTime: 45, portions: 4, ownerUsername: 'anna', shared: true,
  ingredients: [
    { amount: 600, unit: 'G', name: 'chicken thighs' },
    { amount: 1, unit: 'CAN', name: 'coconut milk' },
    { amount: 2, unit: 'TBSP', name: 'curry paste' },
  ],
  steps: [],
};

export const CardView = () => <Recipe recipe={curry} view="card" onSelect={() => {}} />;

export const ListView = () => (
  <ul className="recipe-collection recipe-collection--list" style={{ width: 560 }}>
    <Recipe recipe={curry} view="list" onSelect={() => {}} />
  </ul>
);
