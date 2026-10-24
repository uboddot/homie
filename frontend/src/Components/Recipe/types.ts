import type { Recipe } from '../../model/Recipe';

/** Props every recipe view (card, list item, ...) receives. */
export type RecipeViewProps = {
  recipe: Recipe;
  /** Set when the recipe is clickable, e.g. to open its details. */
  onSelect?: (recipe: Recipe) => void;
};
