import { Minus, Plus } from 'lucide-react';
import { Button } from '@/Components/ui/button';
import './PortionStepper.css';

type PortionStepperProps = {
  value: number;
  onChange: (value: number) => void;
};

/** − / + buttons around the number of portions; never goes below 1. */
export default function PortionStepper({ value, onChange }: PortionStepperProps) {
  return (
    <div className="portion-stepper">
      <Button type="button" variant="outline" size="icon-sm" aria-label="Fewer portions"
        disabled={value <= 1} onClick={() => onChange(value - 1)}>
        <Minus />
      </Button>
      <span className="portion-stepper-value" aria-live="polite">
        {value} {value === 1 ? 'portion' : 'portions'}
      </span>
      <Button type="button" variant="outline" size="icon-sm" aria-label="More portions"
        onClick={() => onChange(value + 1)}>
        <Plus />
      </Button>
    </div>
  );
}
