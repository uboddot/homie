import { useState } from 'react';
import { IngredientList } from 'homie-ui';

function Controlled({ initial }: { initial: { amount: string; unit: string | null; name: string }[] }) {
  const [rows, setRows] = useState(initial);
  return (
    <div style={{ maxWidth: 520 }}>
      <IngredientList rows={rows} onChange={setRows} />
    </div>
  );
}

export const Filled = () => (
  <Controlled
    initial={[
      { amount: '400', unit: 'G', name: 'spaghetti' },
      { amount: '4', unit: 'PIECE', name: 'egg yolks' },
      { amount: '1', unit: 'TSP', name: 'black pepper' },
      { amount: '', unit: null, name: 'salt' },
    ]}
  />
);

export const NewRow = () => (
  <Controlled initial={[{ amount: '', unit: null, name: '' }]} />
);

export const Empty = () => <Controlled initial={[]} />;
