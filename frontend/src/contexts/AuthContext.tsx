import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { apiFetch } from '../api/client';
import type { User } from '../model/User';
import { AuthContext } from './auth';

/**
 * Holds the logged-in user. The JWT itself lives in an HttpOnly cookie the
 * frontend can't read, so on startup we ask the backend who we are (/auth/me).
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiFetch<User>('/auth/me')
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const loggedIn = await apiFetch<User>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
    setUser(loggedIn);
  }, []);

  const register = useCallback(async (username: string, email: string, password: string) => {
    const registered = await apiFetch<User>('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, email, password }),
    });
    setUser(registered);
  }, []);

  const logout = useCallback(async () => {
    await apiFetch<void>('/auth/logout', { method: 'POST' });
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, loading, login, register, logout }),
    [user, loading, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
