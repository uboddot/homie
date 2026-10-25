import { Input, Label } from 'homie-ui';

const field = { display: 'grid', gap: 8, width: 300 } as const;

export const WithLabel = () => (
  <div style={field}>
    <Label htmlFor="recipe-name">Name</Label>
    <Input id="recipe-name" defaultValue="Spaghetti Carbonara" />
  </div>
);

export const Placeholder = () => (
  <div style={field}>
    <Label htmlFor="recipe-time">Preparation time (min)</Label>
    <Input id="recipe-time" type="number" placeholder="30" />
  </div>
);

export const Invalid = () => (
  <div style={field}>
    <Label htmlFor="recipe-portions">Portions</Label>
    <Input id="recipe-portions" type="number" defaultValue="0" aria-invalid />
  </div>
);

export const Disabled = () => (
  <div style={field}>
    <Label htmlFor="recipe-owner">Owner</Label>
    <Input id="recipe-owner" defaultValue="anna" disabled />
  </div>
);
