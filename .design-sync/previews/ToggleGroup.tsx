import { ToggleGroup, ToggleGroupItem } from 'homie-ui';

export const SingleOutline = () => (
  <ToggleGroup type="single" variant="outline" defaultValue="card" aria-label="Recipe view">
    <ToggleGroupItem value="card">Cards</ToggleGroupItem>
    <ToggleGroupItem value="list">List</ToggleGroupItem>
  </ToggleGroup>
);

export const Multiple = () => (
  <ToggleGroup type="multiple" defaultValue={['veggie']} aria-label="Filters">
    <ToggleGroupItem value="veggie">Vegetarian</ToggleGroupItem>
    <ToggleGroupItem value="quick">Under 30 min</ToggleGroupItem>
    <ToggleGroupItem value="shared">Shared</ToggleGroupItem>
  </ToggleGroup>
);
