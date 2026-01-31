import type { Recipe } from '../model/Recipe';
import reactLogo from '../assets/react.svg';

export default function Card(recipe: Recipe) {
  return <div key={recipe.id} className="card">
    
    {/* recipe thumbnail */}
    <img src={reactLogo} alt="Recipe thumbnail" />
    
    {/* recipe name */}
    <h3>{recipe.name}</h3>

    {/* recipe description */}
    {recipe.description}

  </div>;
}