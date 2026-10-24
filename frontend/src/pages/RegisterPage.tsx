import { useState, type FormEvent } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { Button } from '@/Components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/Components/ui/card';
import { Input } from '@/Components/ui/input';
import { Label } from '@/Components/ui/label';
import { ApiError } from '../api/client';
import ResendVerification from '../Components/ResendVerification';
import { useAuth } from '../contexts/auth';
import './AuthPage.css';

export default function RegisterPage() {
  const { user, register } = useAuth();

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [sentTo, setSentTo] = useState<string | null>(null);

  if (user) {
    return <Navigate to="/" replace />;
  }

  if (sentTo) {
    return (
      <div className="auth-page">
        <Card className="auth-card">
          <CardHeader>
            <CardTitle>Check your inbox</CardTitle>
            <CardDescription>
              We sent a confirmation link to <strong>{sentTo}</strong>. Open it to activate your account.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ResendVerification email={sentTo} />
          </CardContent>
          <CardFooter className="auth-footer">
            <span>Already confirmed? <Link to="/login">Log in</Link></span>
          </CardFooter>
        </Card>
      </div>
    );
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await register(username, email, password);
      setSentTo(email);
    } catch (err) {
      // backend sends readable messages for 400 (validation), 409 (taken) and 503 (mail failed)
      setError(err instanceof ApiError ? err.message : 'Registration failed, please try again');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <CardHeader>
          <CardTitle>Create an account</CardTitle>
          <CardDescription>Collect your recipes and share them with others.</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="auth-form" onSubmit={handleSubmit}>
            <div className="auth-field">
              <Label htmlFor="username">Username</Label>
              <Input id="username" autoComplete="username" required
                value={username} onChange={e => setUsername(e.target.value)} />
            </div>
            <div className="auth-field">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" autoComplete="email" required
                value={email} onChange={e => setEmail(e.target.value)} />
            </div>
            <div className="auth-field">
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" autoComplete="new-password" required minLength={8}
                value={password} onChange={e => setPassword(e.target.value)} />
            </div>
            {error && <p className="auth-error" role="alert">{error}</p>}
            <Button type="submit" className="auth-submit" disabled={submitting}>
              {submitting ? 'Creating account...' : 'Register'}
            </Button>
          </form>
        </CardContent>
        <CardFooter className="auth-footer">
          <span>Already have an account? <Link to="/login">Log in</Link></span>
        </CardFooter>
      </Card>
    </div>
  );
}
