import { createContext, useContext } from 'react';
import type { User } from '../model/User';

export type AuthContextValue = {
  user: User | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  /** Creates the account and sends the confirmation mail. Doesn't log in yet. */
  register: (username: string, email: string, password: string) => Promise<void>;
  /** Confirms the email with the token from the mail link and logs the user in. */
  verifyEmail: (token: string) => Promise<void>;
  resendVerification: (email: string) => Promise<void>;
  logout: () => Promise<void>;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside <AuthProvider>');
  }
  return context;
}
