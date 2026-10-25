import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { Toaster } from '@/Components/ui/sonner';
import './App.css';
import Navbar from './Components/Navbar';
import ProtectedRoute from './Components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import VerifyEmailPage from './pages/VerifyEmailPage';
import HomePage from './pages/HomePage';
import RecipesPage from './pages/RecipesPage';
import MealPlanPage from './pages/MealPlanPage';

function AppLayout() {
  return (
    <>
      <Navbar />
      <main>
        <Outlet />
      </main>
    </>
  )
}

function App() {
  return (
    <>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/recipes" element={<RecipesPage />} />
            <Route path="/meal-plan" element={<MealPlanPage />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      {/* light: the app has no dark mode, so toasts shouldn't follow the OS theme */}
      <Toaster position="bottom-right" theme="light" />
    </>
  )
}

export default App
