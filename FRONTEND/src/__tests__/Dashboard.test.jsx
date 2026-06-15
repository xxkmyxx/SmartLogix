import { render, screen, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import Dashboard from '../pages/Dashboard/Dashboard';

vi.mock('../api/axios', () => ({
  default: { get: vi.fn() },
}));

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

const mockData = {
  totalProductos: 42,
  alertasStock: 3,
  totalPedidos: 10,
  pedidosPendientes: 2,
  pedidosEnTransito: 5,
  pedidosEntregados: 3,
};

describe('Dashboard', () => {
  beforeEach(() => vi.clearAllMocks());

  test('muestra "Cargando..." mientras espera respuesta', () => {
    useAuth.mockReturnValue({ user: { role: 'ADMIN' } });
    api.get.mockReturnValue(new Promise(() => {}));
    render(<Dashboard />);
    expect(screen.getByText('Cargando...')).toBeInTheDocument();
  });

  test('muestra las estadísticas al cargar datos', async () => {
    useAuth.mockReturnValue({ user: { role: 'ADMIN' } });
    api.get.mockResolvedValueOnce({ data: mockData });
    render(<Dashboard />);
    await waitFor(() => expect(screen.getByText('42')).toBeInTheDocument());
    expect(screen.getByText('Productos en inventario')).toBeInTheDocument();
    expect(screen.getByText('10')).toBeInTheDocument();
  });

  test('muestra mensaje de error si el API falla', async () => {
    useAuth.mockReturnValue({ user: { role: 'ADMIN' } });
    api.get.mockRejectedValueOnce(new Error('Network Error'));
    render(<Dashboard />);
    await waitFor(() =>
      expect(screen.getByText(/No se pudo cargar el dashboard/i)).toBeInTheDocument()
    );
  });

  test('transportista no ve stats de inventario', async () => {
    useAuth.mockReturnValue({ user: { role: 'TRANSPORTISTA' } });
    api.get.mockResolvedValueOnce({ data: mockData });
    render(<Dashboard />);
    await waitFor(() => expect(screen.getByText('10')).toBeInTheDocument());
    expect(screen.queryByText('Productos en inventario')).not.toBeInTheDocument();
    expect(screen.queryByText('Alertas de stock bajo')).not.toBeInTheDocument();
  });

  test('muestra todos los stats para rol ADMIN', async () => {
    useAuth.mockReturnValue({ user: { role: 'ADMIN' } });
    api.get.mockResolvedValueOnce({ data: mockData });
    render(<Dashboard />);
    await waitFor(() => expect(screen.getByText('Productos en inventario')).toBeInTheDocument());
    expect(screen.getByText('Total de pedidos')).toBeInTheDocument();
    expect(screen.getByText('En tránsito')).toBeInTheDocument();
    expect(screen.getByText('Entregados')).toBeInTheDocument();
  });
});
