import { useState } from 'react';
import { RecipeViewToggle } from 'homie-ui';

function Toggle({ initial }: { initial: 'card' | 'list' }) {
  const [view, setView] = useState(initial);
  return <RecipeViewToggle view={view} onChange={setView} />;
}

export const CardsSelected = () => <Toggle initial="card" />;

export const ListSelected = () => <Toggle initial="list" />;
