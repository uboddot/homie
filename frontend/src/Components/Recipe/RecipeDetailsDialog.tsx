import { Pencil } from 'lucide-react';
import { Button } from '@/Components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/Components/ui/dialog';
import type { Recipe } from '../../model/Recipe';
import RecipeDetails from './RecipeDetails';
import './RecipeDetailsDialog.css';

type RecipeDetailsDialogProps = {
  recipe: Recipe | null;
  onClose: () => void;
  /** Shows an Edit button; only pass it when the user may edit the recipe. */
  onEdit?: (recipe: Recipe) => void;
};

/** Modal with the details of a recipe; open while `recipe` is set. */
export default function RecipeDetailsDialog({ recipe, onClose, onEdit }: RecipeDetailsDialogProps) {
  return (
    <Dialog open={recipe !== null} onOpenChange={open => !open && onClose()}>
      {recipe && (
        <DialogContent className="recipe-details-dialog">
          <DialogHeader>
            <DialogTitle className="recipe-details-dialog-title">{recipe.name}</DialogTitle>
            <DialogDescription>{recipe.description}</DialogDescription>
          </DialogHeader>
          {onEdit && (
            <div className="recipe-details-dialog-actions">
              <Button variant="outline" size="sm" onClick={() => onEdit(recipe)}>
                <Pencil /> Edit
              </Button>
            </div>
          )}
          <RecipeDetails recipe={recipe} />
        </DialogContent>
      )}
    </Dialog>
  );
}
