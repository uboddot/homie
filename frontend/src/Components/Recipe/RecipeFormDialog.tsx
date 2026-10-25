import { useEffect, useState, type FormEvent } from 'react';
import { toast } from 'sonner';
import { Button } from '@/Components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/Components/ui/dialog';
import { Input } from '@/Components/ui/input';
import { Label } from '@/Components/ui/label';
import { Switch } from '@/Components/ui/switch';
import { Textarea } from '@/Components/ui/textarea';
import { apiFetch, ApiError } from '../../api/client';
import LoadingBorder from '../LoadingBorder';
import type { Recipe, RecipeInput } from '../../model/Recipe';
import EditableList from './EditableList';
import IngredientList from './IngredientList';
import { emptyIngredientRow, ingredientError, toIngredientRows, toIngredients, type IngredientRow } from './ingredients';
import NutritionFields from './NutritionFields';
import { emptyNutritionRow, nutritionError, toNutritionInput, toNutritionRow, type NutritionRow } from './nutrition';
import './RecipeFormDialog.css';

type RecipeFormDialogProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSaved: (recipe: Recipe) => void;
  /** The recipe to edit; without it the form creates a new one. */
  recipe?: Recipe | null;
};

/** At least one (empty) row, so the list always shows an input. */
function withRow<T>(items: T[], empty: T) {
  return items.length > 0 ? items : [empty];
}

/** Modal form to create or edit a recipe; the backend makes the logged-in user the owner of a new one. */
export default function RecipeFormDialog({ open, onOpenChange, onSaved, recipe }: RecipeFormDialogProps) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [preparationTime, setPreparationTime] = useState('');
  const [portions, setPortions] = useState('4');
  const [ingredients, setIngredients] = useState<IngredientRow[]>([emptyIngredientRow]);
  const [steps, setSteps] = useState<string[]>(['']);
  const [shared, setShared] = useState(false);
  const [nutrition, setNutrition] = useState<NutritionRow>(emptyNutritionRow);
  // a suggestion stays one (and is recalculated by the backend) until the user changes a value
  const [nutritionTouched, setNutritionTouched] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  // fill the form each time it opens: empty for a new recipe, with the current values when editing
  useEffect(() => {
    if (!open) {
      return;
    }
    setName(recipe?.name ?? '');
    setDescription(recipe?.description ?? '');
    setPreparationTime(recipe ? String(recipe.preparationTime) : '');
    setPortions(recipe ? String(recipe.portions) : '4');
    setIngredients(withRow(toIngredientRows(recipe?.ingredients ?? []), emptyIngredientRow));
    setSteps(withRow(recipe?.steps.map(s => s.instruction) ?? [], ''));
    setShared(recipe?.shared ?? false);
    setNutrition(toNutritionRow(recipe?.nutrition ?? null));
    setNutritionTouched(false);
    setError(null);
  }, [open, recipe]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    const body: RecipeInput = {
      name: name.trim(),
      description: description.trim(),
      preparationTime: preparationTime === '' ? null : Number(preparationTime),
      portions: portions === '' ? null : Number(portions),
      ingredients: toIngredients(ingredients),
      steps: steps.map(s => s.trim()).filter(Boolean),
      shared,
      // only the user's own values are sent; without them the backend suggests (or keeps) a calculated value
      nutrition: nutritionTouched || recipe?.nutrition?.suggested === false ? toNutritionInput(nutrition) : null,
    };
    const invalid = ingredientError(body.ingredients) ?? (nutritionTouched ? nutritionError(nutrition) : null);
    if (invalid) {
      setError(invalid);
      return;
    }

    setSubmitting(true);
    try {
      const saved = recipe
        ? await apiFetch<Recipe>(`/recipes/${recipe.id}`, { method: 'PUT', body: JSON.stringify(body) })
        : await apiFetch<Recipe>('/recipes', { method: 'POST', body: JSON.stringify(body) });
      onSaved(saved);
      onOpenChange(false);
      if (recipe) {
        toast.success(`"${saved.name}" saved`);
      } else {
        toast.success(`"${saved.name}" created`, {
          description: saved.nutrition ? undefined : "Nutrition info couldn't be calculated.",
        });
      }
    } catch (err) {
      setError(err instanceof ApiError && err.status === 401
        ? 'Your session has expired, please log in again'
        : err instanceof ApiError && err.status === 403
          ? 'You can only edit your own recipes'
          : err instanceof Error ? err.message : 'Could not save the recipe');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="recipe-form-dialog">
        <DialogHeader>
          <DialogTitle>{recipe ? 'Edit recipe' : 'New recipe'}</DialogTitle>
          <DialogDescription>
            {recipe ? 'Change your recipe and save it.' : 'Add it to your collection, or share it with everyone.'}
          </DialogDescription>
        </DialogHeader>

        <form className="recipe-form" onSubmit={handleSubmit}>
          <div className="recipe-form-field">
            <Label htmlFor="recipe-name">Name</Label>
            <Input id="recipe-name" required value={name} onChange={e => setName(e.target.value)} />
          </div>

          <div className="recipe-form-field">
            <Label htmlFor="recipe-description">Description</Label>
            <Textarea id="recipe-description" rows={3}
              value={description} onChange={e => setDescription(e.target.value)} />
          </div>

          <div className="recipe-form-row">
            <div className="recipe-form-field recipe-form-number">
              <Label htmlFor="recipe-time">Preparation time (min)</Label>
              <Input id="recipe-time" type="number" min={0} inputMode="numeric"
                value={preparationTime} onChange={e => setPreparationTime(e.target.value)} />
            </div>

            <div className="recipe-form-field recipe-form-number">
              <Label htmlFor="recipe-portions">Portions</Label>
              <Input id="recipe-portions" type="number" min={1} step={1} inputMode="numeric" required
                value={portions} onChange={e => setPortions(e.target.value)} />
            </div>
          </div>

          <IngredientList rows={ingredients} onChange={setIngredients} />

          <NutritionFields value={nutrition} suggested={recipe?.nutrition?.suggested === true && !nutritionTouched}
            onChange={value => { setNutrition(value); setNutritionTouched(true); }} />

          <EditableList id="recipe-step" label="Steps" items={steps} onChange={setSteps}
            placeholder="What to do next" addLabel="Add step" ordered />

          <div className="recipe-form-shared">
            <Switch id="recipe-shared" checked={shared} onCheckedChange={setShared} />
            <Label htmlFor="recipe-shared">Share with everyone</Label>
          </div>

          {error && <p className="recipe-form-error" role="alert">{error}</p>}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
            {/* creating waits for the backend's nutrition lookup (Gemini), which can take a few seconds */}
            <LoadingBorder active={submitting}>
              <Button type="submit" disabled={submitting}>
                {submitting
                  ? recipe ? 'Saving...' : 'Extracting nutrition info...'
                  : recipe ? 'Save changes' : 'Create recipe'}
              </Button>
            </LoadingBorder>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
