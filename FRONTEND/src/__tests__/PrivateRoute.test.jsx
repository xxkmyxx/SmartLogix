import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';
import PrivateRoute from '../components/PrivateRoute/PrivateRoute';

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from '../context/AuthContext';

const wrap = (ui) => render(<MemoryRouter>{ui}</MemoryRouter>);

describe('PrivateRoute', () => {
  test('redirige a /login si no hay token', () => {
    useAuth.mockReturnValue({ token: null, user: null });
    wrap(<PrivateRoute><div>Protegido</div></PrivateRoute>);
    expect(screen.queryByText('Protegido')).not.toBeInTheDocument();
  });

  test('muestra children con token válido', () => {
    useAuth.mockReturnValue({ token: 'tok', user: { role: 'ADMIN' } });
    wrap(<PrivateRoute><div>Protegido</div></PrivateRoute>);
    expect(screen.getByText('Protegido')).toBeInTheDocument();
  });

  test('redirige si el rol no tiene acceso', () => {
    useAuth.mockReturnValue({ token: 'tok', user: { role: 'OPERADOR' } });
    wrap(<PrivateRoute roles={['ADMIN']}><div>Solo Admin</div></PrivateRoute>);
    expect(screen.queryByText('Solo Admin')).not.toBeInTheDocument();
  });

  test('permite acceso si el rol está en la lista', () => {
    useAuth.mockReturnValue({ token: 'tok', user: { role: 'OPERADOR' } });
    wrap(<PrivateRoute roles={['ADMIN', 'OPERADOR']}><div>Permitido</div></PrivateRoute>);
    expect(screen.getByText('Permitido')).toBeInTheDocument();
  });

  test('permite acceso cuando no se especifican roles', () => {
    useAuth.mockReturnValue({ token: 'tok', user: { role: 'TRANSPORTISTA' } });
    wrap(<PrivateRoute><div>Sin restricción</div></PrivateRoute>);
    expect(screen.getByText('Sin restricción')).toBeInTheDocument();
  });
});
