import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/Components/ui/dialog';
import type { Recipe } from '../../model/Recipe';
import RecipeDetails from './RecipeDetails';
import './RecipeDetailsDialog.css';

/** Modal with the details of a recipe; open while `recipe` is set. */
export default function RecipeDetailsDialog({ recipe, onClose }: { recipe: Recipe | null; onClose: () => void }) {
  return (
    <Dialog open={recipe !== null} onOpenChange={open => !open && onClose()}>
      {recipe && (
        <DialogContent className="recipe-details-dialog">
          <DialogHeader>
            <DialogTitle className="recipe-details-dialog-title">{recipe.name}</DialogTitle>
            <DialogDescription>{recipe.description}</DialogDescription>
          </DialogHeader>
          <RecipeDetails recipe={recipe} />
        </DialogContent>
      )}
    </Dialog>
  );
}
