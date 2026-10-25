import { Label, Textarea } from 'homie-ui';

export const WithLabel = () => (
  <div style={{ display: 'grid', gap: 8, width: 360 }}>
    <Label htmlFor="description">Description</Label>
    <Textarea id="description" defaultValue="Creamy Roman pasta with egg, pecorino and crispy guanciale." />
  </div>
);

export const Placeholder = () => (
  <div style={{ display: 'grid', gap: 8, width: 360 }}>
    <Label htmlFor="notes">Notes</Label>
    <Textarea id="notes" placeholder="What makes this recipe special?" />
  </div>
);

export const Disabled = () => (
  <div style={{ width: 360 }}>
    <Textarea aria-label="Description" defaultValue="Only the owner can edit this." disabled />
  </div>
);
