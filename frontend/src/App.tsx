import { useEffect, useState } from 'react'
import './App.css'

function App() {

  const [recipes, setRecipes] = useState([]);
  
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
      <div>
        {recipes.map( recipe =>
          <div key={recipe.id}>
            {recipe.name}
          </div>
        )}
      </div>
    </>
  )
}

export default App
