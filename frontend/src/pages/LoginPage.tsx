import { useState, type FormEvent } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Button } from '@/Components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/Components/ui/card';
import { Input } from '@/Components/ui/input';
import { Label } from '@/Components/ui/label';
import { ApiError } from '../api/client';
import { useAuth } from '../contexts/auth';
import './AuthPage.css';

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: Location } | null)?.from?.pathname ?? '/';

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (user) {
    return <Navigate to={from} replace />;
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(username, password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err instanceof ApiError && err.status === 401
        ? 'Wrong username or password'
        : 'Login failed, please try again');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <CardHeader>
          <CardTitle>Log in to Homie</CardTitle>
          <CardDescription>Your recipes, and the ones others share.</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="auth-form" onSubmit={handleSubmit}>
            <div className="auth-field">
              <Label htmlFor="username">Username</Label>
              <Input id="username" autoComplete="username" required
                value={username} onChange={e => setUsername(e.target.value)} />
            </div>
            <div className="auth-field">
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" autoComplete="current-password" required
                value={password} onChange={e => setPassword(e.target.value)} />
            </div>
            {error && <p className="auth-error" role="alert">{error}</p>}
            <Button type="submit" className="auth-submit" disabled={submitting}>
              {submitting ? 'Logging in...' : 'Log in'}
            </Button>
          </form>
        </CardContent>
        <CardFooter className="auth-footer">
          <span>No account yet? <Link to="/register">Register</Link></span>
        </CardFooter>
      </Card>
    </div>
  );
}
