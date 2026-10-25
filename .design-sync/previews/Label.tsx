import { Input, Label, Switch } from 'homie-ui';

export const ForInput = () => (
  <div style={{ display: 'grid', gap: 8, width: 300 }}>
    <Label htmlFor="email">Email</Label>
    <Input id="email" type="email" placeholder="you@example.com" />
  </div>
);

export const ForSwitch = () => (
  <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
    <Switch id="shared" defaultChecked />
    <Label htmlFor="shared">Share with other users</Label>
  </div>
);
