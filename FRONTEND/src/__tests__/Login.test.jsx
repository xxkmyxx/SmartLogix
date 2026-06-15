import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import Login from '../pages/Login/Login';
import { AuthProvider } from '../context/AuthContext';

vi.mock('../api/axios', () => ({
  default: { post: vi.fn() },
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

import api from '../api/axios';
import { MemoryRouter } from 'react-router-dom';

const renderLogin = () =>
  render(
    <MemoryRouter>
      <AuthProvider>
        <Login />
      </AuthProvider>
    </MemoryRouter>
  );

describe('Login', () => {
  beforeEach(() => vi.clearAllMocks());

  test('renderiza campos de email y contraseña', () => {
    renderLogin();
    expect(screen.getByPlaceholderText('correo@ejemplo.com')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('••••••••')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /ingresar/i })).toBeInTheDocument();
  });

  test('muestra el título SmartLogix', () => {
    renderLogin();
    expect(screen.getByText('SmartLogix')).toBeInTheDocument();
  });

  test('login exitoso redirige a /', async () => {
    api.post.mockResolvedValueOnce({ data: { token: 'eyJ.eyJ.sig', rol: 'ADMIN' } });
    renderLogin();
    fireEvent.change(screen.getByPlaceholderText('correo@ejemplo.com'), {
      target: { value: 'admin@test.cl' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: '123456' },
    });
    fireEvent.click(screen.getByRole('button', { name: /ingresar/i }));
    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/'));
  });

  test('muestra error si las credenciales son incorrectas', async () => {
    api.post.mockRejectedValueOnce({
      response: { data: { mensaje: 'Credenciales inválidas' } },
    });
    renderLogin();
    fireEvent.change(screen.getByPlaceholderText('correo@ejemplo.com'), {
      target: { value: 'malo@test.cl' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'wrong' },
    });
    fireEvent.click(screen.getByRole('button', { name: /ingresar/i }));
    await waitFor(() =>
      expect(screen.getByText('Credenciales inválidas')).toBeInTheDocument()
    );
  });

  test('deniega acceso a rol CLIENTE', async () => {
    api.post.mockResolvedValueOnce({ data: { token: 'eyJ.eyJ.sig', rol: 'CLIENTE' } });
    renderLogin();
    fireEvent.change(screen.getByPlaceholderText('correo@ejemplo.com'), {
      target: { value: 'cliente@test.cl' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: '123456' },
    });
    fireEvent.click(screen.getByRole('button', { name: /ingresar/i }));
    await waitFor(() =>
      expect(screen.getByText(/Acceso denegado/i)).toBeInTheDocument()
    );
  });

  test('botón muestra "Ingresando..." mientras carga', async () => {
    api.post.mockReturnValue(new Promise(() => {}));
    renderLogin();
    fireEvent.change(screen.getByPlaceholderText('correo@ejemplo.com'), {
      target: { value: 'admin@test.cl' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: '123456' },
    });
    fireEvent.click(screen.getByRole('button', { name: /ingresar/i }));
    expect(screen.getByText('Ingresando...')).toBeInTheDocument();
  });
});
