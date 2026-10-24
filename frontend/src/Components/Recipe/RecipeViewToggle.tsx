import { ToggleGroup, ToggleGroupItem } from '@/Components/ui/toggle-group';
import type { RecipeView } from '../../model/Recipe';

export default function RecipeViewToggle({ view, onChange }: { view: RecipeView; onChange: (view: RecipeView) => void }) {
  return (
    <ToggleGroup
      type="single"
      variant="outline"
      value={view}
      // radix sends '' when the active item is clicked again; keep the current view then
      onValueChange={value => value && onChange(value as RecipeView)}
      aria-label="Recipe view"
    >
      <ToggleGroupItem value="card" aria-label="Show as cards">Cards</ToggleGroupItem>
      <ToggleGroupItem value="list" aria-label="Show as list">List</ToggleGroupItem>
    </ToggleGroup>
  );
}
