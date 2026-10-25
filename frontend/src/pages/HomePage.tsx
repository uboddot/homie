import { Link } from 'react-router-dom';
import { Button } from '@/Components/ui/button';
import { useAuth } from '../contexts/auth';
import './HomePage.css';

export default function HomePage() {
  const { user } = useAuth();

  return (
    <div className="page home">
      <h1 className="home-title">Welcome, {user?.username}</h1>
      <Button asChild>
        <Link to="/recipes">Go to recipes</Link>
      </Button>
    </div>
  )
}
