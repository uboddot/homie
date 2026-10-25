import type { ReactNode } from 'react';
import './LoadingBorder.css';

type LoadingBorderProps = {
  active: boolean;
  children: ReactNode;
};

/** Wraps e.g. a button; while active, a highlighted arc runs round and round its border. */
export default function LoadingBorder({ active, children }: LoadingBorderProps) {
  return (
    <span className={active ? 'loading-border loading-border--active' : 'loading-border'} aria-busy={active}>
      {children}
    </span>
  );
}
