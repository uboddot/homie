import { useState } from 'react';
import { Check, ChevronsUpDown } from 'lucide-react';
import { Button } from '@/Components/ui/button';
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from '@/Components/ui/command';
import { Popover, PopoverContent, PopoverTrigger } from '@/Components/ui/popover';
import type { Recipe } from '../../model/Recipe';
import './RecipePicker.css';

type RecipePickerProps = {
  /** e.g. "Breakfast", used in the placeholder and for screen readers */
  label: string;
  recipes: Recipe[];
  recipeId: number | null;
  /** shown if the recipe isn't in `recipes` (anymore), e.g. it was unshared */
  recipeName: string | null;
  /** the selected days have different recipes in this slot */
  mixed: boolean;
  disabled?: boolean;
  onPick: (recipeId: number) => void;
};

/** Search-as-you-type dropdown of the recipes, filtered by name. */
export default function RecipePicker({ label, recipes, recipeId, recipeName, mixed, disabled, onPick }: RecipePickerProps) {
  const [open, setOpen] = useState(false);

  const current = recipes.find(recipe => recipe.id === recipeId)?.name ?? recipeName;
  const placeholder = mixed ? 'Varies — pick one to set for all' : `Add ${label.toLowerCase()}…`;

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          role="combobox"
          aria-expanded={open}
          aria-label={`${label}: ${current ?? 'none'}`}
          className="recipe-picker-trigger"
          disabled={disabled}
        >
          <span className={current ? 'recipe-picker-value' : 'recipe-picker-placeholder'}>{current ?? placeholder}</span>
          <ChevronsUpDown className="recipe-picker-chevron" />
        </Button>
      </PopoverTrigger>
      <PopoverContent
        className="recipe-picker-content"
        align="start"
        // Esc closes only the dropdown, not the day selection of the page
        onEscapeKeyDown={event => event.stopPropagation()}
      >
        {/* names can repeat, so items are keyed by id and the search only looks at the name */}
        <Command
          filter={(_value, search, keywords) =>
            keywords?.some(keyword => keyword.toLowerCase().includes(search.trim().toLowerCase())) ? 1 : 0}
        >
          <CommandInput placeholder="Search recipes…" />
          <CommandList>
            <CommandEmpty>No recipe found.</CommandEmpty>
            <CommandGroup>
              {recipes.map(recipe => (
                <CommandItem
                  key={recipe.id}
                  value={String(recipe.id)}
                  keywords={[recipe.name]}
                  onSelect={() => {
                    onPick(recipe.id);
                    setOpen(false);
                  }}
                >
                  <Check className={recipe.id === recipeId ? 'recipe-picker-check' : 'recipe-picker-check is-hidden'} />
                  <span className="recipe-picker-name">{recipe.name}</span>
                  {recipe.ownerUsername && <span className="recipe-picker-owner">by {recipe.ownerUsername}</span>}
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}
