import { useState, type FormEvent } from 'react';
import { Button } from '@/Components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/Components/ui/dialog';
import { Input } from '@/Components/ui/input';
import { Label } from '@/Components/ui/label';
import { Switch } from '@/Components/ui/switch';
import { Textarea } from '@/Components/ui/textarea';
import { apiFetch, ApiError } from '../../api/client';
import type { NewRecipe, Recipe } from '../../model/Recipe';
import EditableList from './EditableList';
import './CreateRecipeDialog.css';

type CreateRecipeDialogProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onCreated: (recipe: Recipe) => void;
};

/** Modal form for a new recipe; the backend makes the logged-in user its owner. */
export default function CreateRecipeDialog({ open, onOpenChange, onCreated }: CreateRecipeDialogProps) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [preparationTime, setPreparationTime] = useState('');
  const [ingredients, setIngredients] = useState<string[]>(['']);
  const [steps, setSteps] = useState<string[]>(['']);
  const [shared, setShared] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function reset() {
    setName('');
    setDescription('');
    setPreparationTime('');
    setIngredients(['']);
    setSteps(['']);
    setShared(false);
    setError(null);
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    const body: NewRecipe = {
      name: name.trim(),
      description: description.trim(),
      preparationTime: preparationTime === '' ? null : Number(preparationTime),
      ingredients: ingredients.map(i => i.trim()).filter(Boolean),
      steps: steps.map(s => s.trim()).filter(Boolean),
      shared,
    };
    if (body.ingredients.length === 0) {
      setError('Add at least one ingredient');
      return;
    }

    setSubmitting(true);
    try {
      const created = await apiFetch<Recipe>('/recipes', { method: 'POST', body: JSON.stringify(body) });
      onCreated(created);
      reset();
      onOpenChange(false);
    } catch (err) {
      setError(err instanceof ApiError && err.status === 401
        ? 'Your session has expired, please log in again'
        : err instanceof Error ? err.message : 'Could not save the recipe');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="create-recipe-dialog">
        <DialogHeader>
          <DialogTitle>New recipe</DialogTitle>
          <DialogDescription>Add it to your collection, or share it with everyone.</DialogDescription>
        </DialogHeader>

        <form className="create-recipe-form" onSubmit={handleSubmit}>
          <div className="create-recipe-field">
            <Label htmlFor="recipe-name">Name</Label>
            <Input id="recipe-name" required value={name} onChange={e => setName(e.target.value)} />
          </div>

          <div className="create-recipe-field">
            <Label htmlFor="recipe-description">Description</Label>
            <Textarea id="recipe-description" rows={3}
              value={description} onChange={e => setDescription(e.target.value)} />
          </div>

          <div className="create-recipe-field create-recipe-time">
            <Label htmlFor="recipe-time">Preparation time (min)</Label>
            <Input id="recipe-time" type="number" min={0} inputMode="numeric"
              value={preparationTime} onChange={e => setPreparationTime(e.target.value)} />
          </div>

          <EditableList id="recipe-ingredient" label="Ingredients" items={ingredients} onChange={setIngredients}
            placeholder="e.g. 200 g flour" addLabel="Add ingredient" />

          <EditableList id="recipe-step" label="Steps" items={steps} onChange={setSteps}
            placeholder="What to do next" addLabel="Add step" ordered />

          <div className="create-recipe-shared">
            <Switch id="recipe-shared" checked={shared} onCheckedChange={setShared} />
            <Label htmlFor="recipe-shared">Share with everyone</Label>
          </div>

          {error && <p className="create-recipe-error" role="alert">{error}</p>}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
            <Button type="submit" disabled={submitting}>{submitting ? 'Saving...' : 'Create recipe'}</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
