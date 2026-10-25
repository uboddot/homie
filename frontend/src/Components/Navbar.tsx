import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { Menu } from 'lucide-react';
import { Button } from '@/Components/ui/button';
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from '@/Components/ui/sheet';
import { useAuth } from '../contexts/auth';
import './Navbar.css';

const navLinks = [
  { to: '/recipes', label: 'Recipes' },
  { to: '/meal-plan', label: 'Meal plan' },
];

function NavLinks({ className, onNavigate }: { className: string; onNavigate?: () => void }) {
  return (
    <ul className={className}>
      {navLinks.map(link => (
        <li key={link.to}>
          <NavLink
            to={link.to}
            end
            className={({ isActive }) => isActive ? 'navbar-link active' : 'navbar-link'}
            onClick={onNavigate}
          >
            {link.label}
          </NavLink>
        </li>
      ))}
    </ul>
  );
}

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  async function handleLogout() {
    setMenuOpen(false);
    await logout();
    navigate('/login', { replace: true });
  }

  return (
    <header className="navbar">
      <nav className="navbar-inner">
        <Link to="/" className="navbar-brand">Homie</Link>
        <NavLinks className="navbar-links" />
        {user && (
          <div className="navbar-user">
            <span>{user.username}</span>
            <Button variant="outline" size="sm" onClick={handleLogout}>Log out</Button>
          </div>
        )}

        <Sheet open={menuOpen} onOpenChange={setMenuOpen}>
          <SheetTrigger asChild>
            <Button variant="ghost" size="icon" className="navbar-menu-button" aria-label="Open menu">
              <Menu />
            </Button>
          </SheetTrigger>
          <SheetContent side="right" aria-describedby={undefined}>
            <SheetHeader>
              <SheetTitle>Homie</SheetTitle>
            </SheetHeader>
            <NavLinks className="navbar-mobile-links" onNavigate={() => setMenuOpen(false)} />
            {user && (
              <div className="navbar-mobile-user">
                <span>{user.username}</span>
                <Button variant="outline" size="sm" onClick={handleLogout}>Log out</Button>
              </div>
            )}
          </SheetContent>
        </Sheet>
      </nav>
    </header>
  );
}
