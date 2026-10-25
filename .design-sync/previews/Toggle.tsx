import { Heart, Star } from 'lucide-react';
import { Toggle } from 'homie-ui';

export const Default = () => (
  <div style={{ display: 'flex', gap: 8 }}>
    <Toggle aria-label="Favorite"><Heart /> Favorite</Toggle>
    <Toggle aria-label="Favorite" defaultPressed><Heart /> Favorite</Toggle>
  </div>
);

export const Outline = () => (
  <div style={{ display: 'flex', gap: 8 }}>
    <Toggle variant="outline" aria-label="Starred"><Star /> Starred</Toggle>
    <Toggle variant="outline" aria-label="Starred" defaultPressed><Star /> Starred</Toggle>
  </div>
);

export const Sizes = () => (
  <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
    <Toggle variant="outline" size="sm">Small</Toggle>
    <Toggle variant="outline">Default</Toggle>
    <Toggle variant="outline" size="lg">Large</Toggle>
  </div>
);
