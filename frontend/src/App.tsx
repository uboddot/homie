import { useEffect, useState } from 'react';
import './App.css';
import type { Recipe } from './model/Recipe';
import Card from './Components/Card';

function App() {

  const [recipes, setRecipes] = useState<Recipe[]>([]);
  
  useEffect(() => {
    fetch('http://localhost:8080/recipes')
      .then(response => response.json())
      .then(data => {
        console.log(data);
        setRecipes(data);
      })
      .catch(err => console.log(err));
  }, [])

  if(recipes.length < 1) {
    return (
      <div>
        Loading...
      </div>
    )
  }

  return (
    <>
      <div className="card-container">
        {recipes.map( recipe =>
          <Card key={recipe.id} {...recipe} />
        )}
      </div>
    </>
  )
}

export default App