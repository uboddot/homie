import { useState, type FormEvent } from 'react';
import { Button } from '@/Components/ui/button';
import { Input } from '@/Components/ui/input';
import { Label } from '@/Components/ui/label';
import { ApiError } from '../api/client';
import { useAuth } from '../contexts/auth';
import './ResendVerification.css';

/**
 * Sends a new confirmation mail. With a known `email` it is just a button,
 * otherwise it asks for the address first.
 */
export default function ResendVerification({ email: knownEmail }: { email?: string }) {
  const { resendVerification } = useAuth();
  const [email, setEmail] = useState(knownEmail ?? '');
  const [status, setStatus] = useState<'idle' | 'sending' | 'sent' | 'error'>('idle');
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setStatus('sending');
    setError(null);
    try {
      await resendVerification(email);
      setStatus('sent');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not send the email, please try again');
      setStatus('error');
    }
  }

  return (
    <form className="resend-verification" onSubmit={handleSubmit}>
      {!knownEmail && (
        <div className="auth-field">
          <Label htmlFor="resend-email">Email</Label>
          <Input id="resend-email" type="email" autoComplete="email" required
            value={email} onChange={e => setEmail(e.target.value)} />
        </div>
      )}
      <Button type="submit" variant="outline" className="auth-submit" disabled={status === 'sending'}>
        {status === 'sending' ? 'Sending...' : 'Resend confirmation email'}
      </Button>
      {status === 'sent' && <p className="resend-verification-info" role="status">A new link is on its way.</p>}
      {error && <p className="auth-error" role="alert">{error}</p>}
    </form>
  );
}
