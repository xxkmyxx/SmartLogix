import { render, screen, act } from '@testing-library/react';
import { AuthProvider, useAuth } from '../context/AuthContext';

const fakePayload = btoa(JSON.stringify({ email: 'test@test.cl', role: 'ADMIN', userId: 1 }));
const fakeToken = `header.${fakePayload}.signature`;

function ConsumerComponent() {
  const { user, login, logout } = useAuth();
  return (
    <div>
      <span data-testid="email">{user?.email ?? 'sin-usuario'}</span>
      <span data-testid="role">{user?.role ?? ''}</span>
      <button onClick={() => login(fakeToken)}>login</button>
      <button onClick={logout}>logout</button>
    </div>
  );
}

describe('AuthContext', () => {
  beforeEach(() => localStorage.clear());

  test('usuario es null sin token', () => {
    render(<AuthProvider><ConsumerComponent /></AuthProvider>);
    expect(screen.getByTestId('email').textContent).toBe('sin-usuario');
  });

  test('login guarda el token y expone el usuario', () => {
    render(<AuthProvider><ConsumerComponent /></AuthProvider>);
    act(() => screen.getByText('login').click());
    expect(screen.getByTestId('email').textContent).toBe('test@test.cl');
    expect(screen.getByTestId('role').textContent).toBe('ADMIN');
  });

  test('login persiste el token en localStorage', () => {
    render(<AuthProvider><ConsumerComponent /></AuthProvider>);
    act(() => screen.getByText('login').click());
    expect(localStorage.getItem('token')).toBe(fakeToken);
  });

  test('logout limpia el usuario', () => {
    render(<AuthProvider><ConsumerComponent /></AuthProvider>);
    act(() => screen.getByText('login').click());
    act(() => screen.getByText('logout').click());
    expect(screen.getByTestId('email').textContent).toBe('sin-usuario');
  });

  test('logout limpia localStorage', () => {
    render(<AuthProvider><ConsumerComponent /></AuthProvider>);
    act(() => screen.getByText('login').click());
    act(() => screen.getByText('logout').click());
    expect(localStorage.getItem('token')).toBeNull();
  });
});
