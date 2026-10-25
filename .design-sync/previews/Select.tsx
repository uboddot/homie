import { Label, Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from 'homie-ui';

const Units = () => (
  <SelectContent>
    <SelectItem value="G">g</SelectItem>
    <SelectItem value="KG">kg</SelectItem>
    <SelectItem value="ML">ml</SelectItem>
    <SelectItem value="TSP">tsp</SelectItem>
    <SelectItem value="TBSP">tbsp</SelectItem>
  </SelectContent>
);

export const WithValue = () => (
  <div style={{ display: 'grid', gap: 8, width: 200 }}>
    <Label>Unit</Label>
    <Select defaultValue="G">
      <SelectTrigger style={{ width: '100%' }} aria-label="Unit"><SelectValue /></SelectTrigger>
      <Units />
    </Select>
  </div>
);

export const Placeholder = () => (
  <div style={{ width: 200 }}>
    <Select>
      <SelectTrigger style={{ width: '100%' }} aria-label="Unit"><SelectValue placeholder="Choose a unit" /></SelectTrigger>
      <Units />
    </Select>
  </div>
);

export const Small = () => (
  <Select defaultValue="TBSP">
    <SelectTrigger size="sm" aria-label="Unit"><SelectValue /></SelectTrigger>
    <Units />
  </Select>
);
