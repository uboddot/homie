import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/Components/ui/card';
import { ApiError } from '../api/client';
import ResendVerification from '../Components/ResendVerification';
import { useAuth } from '../contexts/auth';
import './AuthPage.css';

/** Target of the link in the confirmation mail: confirms the email, logs in and goes to the recipes. */
export default function VerifyEmailPage() {
  const { verifyEmail } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const [error, setError] = useState<string | null>(token ? null : 'This link is incomplete.');
  // the token works only once, so StrictMode's second effect run must not send it again
  const sent = useRef(false);

  useEffect(() => {
    if (!token || sent.current) return;
    sent.current = true;
    verifyEmail(token)
      .then(() => navigate('/', { replace: true }))
      .catch(err => setError(err instanceof ApiError ? err.message : 'Confirmation failed, please try again'));
  }, [token, verifyEmail, navigate]);

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <CardHeader>
          <CardTitle>{error ? 'Confirmation failed' : 'Confirming your email...'}</CardTitle>
          {error && <CardDescription>{error} Request a new link below.</CardDescription>}
        </CardHeader>
        {error && (
          <CardContent>
            <ResendVerification />
          </CardContent>
        )}
        <CardFooter className="auth-footer">
          <span>Already confirmed? <Link to="/login">Log in</Link></span>
        </CardFooter>
      </Card>
    </div>
  );
}
