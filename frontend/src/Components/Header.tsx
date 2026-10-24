import { useNavigate } from 'react-router-dom';
import { Button } from '@/Components/ui/button';
import { useAuth } from '../contexts/auth';
import './Header.css';

export default function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  return (
    <header className="header">
      <span className="header-title">Homie</span>
      {user && (
        <div className="header-user">
          <span>{user.username}</span>
          <Button variant="outline" size="sm" onClick={handleLogout}>Log out</Button>
        </div>
      )}
    </header>
  );
}
