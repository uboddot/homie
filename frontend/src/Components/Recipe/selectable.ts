import type { HTMLAttributes, KeyboardEvent } from 'react';
import type { Recipe } from '../../model/Recipe';

/**
 * Props that make a view's root element behave like a button (click, Enter/Space, screen readers).
 * Returns nothing when the recipe isn't selectable.
 */
export function selectableProps(recipe: Recipe, onSelect?: (recipe: Recipe) => void): HTMLAttributes<HTMLElement> {
  if (!onSelect) {
    return {};
  }
  return {
    role: 'button',
    tabIndex: 0,
    'aria-label': `Show details of ${recipe.name}`,
    onClick: () => onSelect(recipe),
    onKeyDown: (event: KeyboardEvent) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        onSelect(recipe);
      }
    },
  };
}
