import { Label, Switch } from 'homie-ui';

const row = { display: 'flex', gap: 8, alignItems: 'center' } as const;

export const States = () => (
  <div style={{ display: 'grid', gap: 12 }}>
    <div style={row}><Switch id="s-on" defaultChecked /><Label htmlFor="s-on">Shared</Label></div>
    <div style={row}><Switch id="s-off" /><Label htmlFor="s-off">Private</Label></div>
  </div>
);

export const Disabled = () => (
  <div style={{ display: 'grid', gap: 12 }}>
    <div style={row}><Switch id="d-on" defaultChecked disabled /><Label htmlFor="d-on">Shared (locked)</Label></div>
    <div style={row}><Switch id="d-off" disabled /><Label htmlFor="d-off">Private (locked)</Label></div>
  </div>
);
