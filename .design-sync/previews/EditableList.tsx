import { useState } from 'react';
import { EditableList } from 'homie-ui';

function Controlled(props: { initial: string[]; label: string; ordered?: boolean; placeholder?: string; addLabel?: string }) {
  const { initial, ...rest } = props;
  const [items, setItems] = useState(initial);
  return (
    <div style={{ maxWidth: 520 }}>
      <EditableList id="preview-list" items={items} onChange={setItems} {...rest} />
    </div>
  );
}

export const Steps = () => (
  <Controlled
    label="Steps"
    ordered
    placeholder="Describe this step"
    addLabel="Add step"
    initial={[
      'Boil the spaghetti in well-salted water until al dente.',
      'Fry the guanciale in a dry pan until crisp.',
      'Whisk the yolks with the pecorino and pepper.',
    ]}
  />
);

export const Unordered = () => (
  <Controlled label="Tags" addLabel="Add tag" placeholder="e.g. vegetarian" initial={['pasta', 'quick']} />
);

export const Empty = () => <Controlled label="Steps" ordered addLabel="Add step" initial={[]} />;
