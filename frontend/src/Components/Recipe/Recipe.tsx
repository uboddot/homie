import type { ComponentType } from 'react';
import type { RecipeView } from '../../model/Recipe';
import type { RecipeViewProps } from './types';
import RecipeCard from './RecipeCard';
import RecipeListItem from './RecipeListItem';

/** All available displays of a recipe. Add new views (e.g. detail) here. */
const views: Record<RecipeView, ComponentType<RecipeViewProps>> = {
  card: RecipeCard,
  list: RecipeListItem,
};

/** Renders a recipe in the requested view; pages use this instead of a specific view. */
export default function Recipe({ recipe, onSelect, view = 'card' }: RecipeViewProps & { view?: RecipeView }) {
  const View = views[view];
  return <View recipe={recipe} onSelect={onSelect} />;
}
