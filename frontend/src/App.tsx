import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import './App.css';
import type { Recipe } from './model/Recipe';
import Card from './Components/Card';
import Login from './pages/Login';
import ProtectedRoute from './components/ProtectedRoute';
import { useAuth } from './contexts/AuthContext';

function RecipesPage() {
  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const { logout } = useAuth();

  useEffect(() => {
    fetch('http://localhost:8080/recipes', {
      credentials: 'include',
    })
      .then(response => response.json())
      .then(data => {
        console.log(data);
        setRecipes(data);
      })
      .catch(err => console.log(err));
  }, [])

  async function handleLogout() {
    await logout();
  }

  if(recipes.length < 1) {
    return (
      <div>
        Loading...
      </div>
    )
  }

  return (
    <>
      <div style={{ padding: '1rem', textAlign: 'right' }}>
        <button onClick={handleLogout} style={{ padding: '0.5rem 1rem', cursor: 'pointer' }}>
          Logout
        </button>
      </div>
      <div className="card-container">
        {recipes.map( recipe =>
          <Card key={recipe.id} {...recipe} />
        )}
      </div>
    </>
  )
}

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/recipes"
          element={
            <ProtectedRoute>
              <RecipesPage />
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<Navigate to="/recipes" replace />} />
      </Routes>
    </BrowserRouter>
  );
}