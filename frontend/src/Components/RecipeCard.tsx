import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/Components/ui/card';
import type { Recipe } from '../model/Recipe';
import reactLogo from '../assets/react.svg';
import './RecipeCard.css';

export default function RecipeCard(recipe: Recipe) {
  return (
    <Card className="recipe-card">
      <CardHeader>
        {/* recipe thumbnail */}
        <img className="recipe-card-image" src={reactLogo} alt="Recipe thumbnail" />

        <CardTitle>{recipe.name}</CardTitle>
        <CardDescription>{recipe.description}</CardDescription>
      </CardHeader>
      <CardContent className="recipe-card-meta">
        <span>{recipe.preparationTime} min</span>
        <span>
          by {recipe.ownerUsername ?? 'unknown'}{recipe.shared ? ' · shared' : ''}
        </span>
      </CardContent>
    </Card>
  );
}
