import { Plus, X } from 'lucide-react';
import { Button } from '@/Components/ui/button';
import { Input } from '@/Components/ui/input';
import { Label } from '@/Components/ui/label';
import './EditableList.css';

type EditableListProps = {
  id: string;
  label: string;
  items: string[];
  onChange: (items: string[]) => void;
  placeholder?: string;
  addLabel?: string;
  ordered?: boolean; // numbers the rows, e.g. for steps
};

/** Form field for a list of texts (ingredients, steps): one input per row, rows can be added and removed. */
export default function EditableList({ id, label, items, onChange, placeholder, addLabel = 'Add', ordered = false }: EditableListProps) {
  function update(index: number, value: string) {
    onChange(items.map((item, i) => (i === index ? value : item)));
  }

  function remove(index: number) {
    onChange(items.filter((_, i) => i !== index));
  }

  return (
    <fieldset className="editable-list">
      <Label asChild><legend>{label}</legend></Label>
      {items.map((item, index) => (
        // index keys are fine: rows are fully controlled and have no own state
        <div className="editable-list-row" key={index}>
          {ordered && <span className="editable-list-number">{index + 1}.</span>}
          <Input
            id={`${id}-${index}`}
            aria-label={`${label} ${index + 1}`}
            placeholder={placeholder}
            value={item}
            onChange={e => update(index, e.target.value)}
          />
          <Button type="button" variant="ghost" size="icon-sm" aria-label={`Remove ${label.toLowerCase()} ${index + 1}`}
            onClick={() => remove(index)}>
            <X />
          </Button>
        </div>
      ))}
      <Button type="button" variant="outline" size="sm" className="editable-list-add" onClick={() => onChange([...items, ''])}>
        <Plus /> {addLabel}
      </Button>
    </fieldset>
  );
}
