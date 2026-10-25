import { Plus, Pencil, Trash2 } from 'lucide-react';
import { Button } from 'homie-ui';

export const Variants = () => (
  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, alignItems: 'center' }}>
    <Button>Save recipe</Button>
    <Button variant="secondary">Cancel</Button>
    <Button variant="outline">Edit</Button>
    <Button variant="ghost">Skip</Button>
    <Button variant="destructive">Delete</Button>
    <Button variant="link">Reset</Button>
  </div>
);

export const Sizes = () => (
  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, alignItems: 'center' }}>
    <Button size="xs">Extra small</Button>
    <Button size="sm">Small</Button>
    <Button>Default</Button>
    <Button size="lg">Large</Button>
  </div>
);

export const WithIcon = () => (
  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, alignItems: 'center' }}>
    <Button size="lg"><Plus /> New recipe</Button>
    <Button variant="outline" size="sm"><Pencil /> Edit</Button>
    <Button variant="outline" size="sm"><Plus /> Add ingredient</Button>
  </div>
);

export const IconOnly = () => (
  <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
    <Button size="icon-sm" variant="outline" aria-label="Add"><Plus /></Button>
    <Button size="icon" variant="outline" aria-label="Edit"><Pencil /></Button>
    <Button size="icon-lg" variant="destructive" aria-label="Delete"><Trash2 /></Button>
  </div>
);

export const Disabled = () => (
  <div style={{ display: 'flex', gap: 12 }}>
    <Button disabled>Saving…</Button>
    <Button variant="outline" disabled>Edit</Button>
  </div>
);
