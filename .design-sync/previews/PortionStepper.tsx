import { useState } from 'react';
import { PortionStepper } from 'homie-ui';

function Stepper({ initial }: { initial: number }) {
  const [value, setValue] = useState(initial);
  return <PortionStepper value={value} onChange={setValue} />;
}

export const FourPortions = () => <Stepper initial={4} />;

// At 1 portion the minus button is disabled.
export const Minimum = () => <Stepper initial={1} />;
