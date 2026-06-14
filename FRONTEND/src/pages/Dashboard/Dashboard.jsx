import { useEffect, useState } from 'react';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import './Dashboard.css';

const StatCard = ({ label, value, color }) => (
  <div className="stat-card" style={{ borderTopColor: color }}>
    <p className="stat-value" style={{ color }}>{value < 0 ? '—' : value}</p>
    <p className="stat-label">{label}</p>
  </div>
);

export default function Dashboard() {
  const { user } = useAuth();
  const esTransportista = user?.role === 'TRANSPORTISTA';
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/api/dashboard')
      .then((r) => setData(r.data))
      .catch(() => setError('No se pudo cargar el dashboard. Verifica que los servicios estén activos.'));
  }, []);

  return (
    <div className="page">
      <h1 className="page-title">Dashboard</h1>
      {error && <p className="error-msg">{error}</p>}
      {data && (
        <>
          <div className="stats-grid">
            {!esTransportista && <StatCard label="Productos en inventario" value={data.totalProductos} color="#8B4513" />}
            {!esTransportista && <StatCard label="Alertas de stock bajo" value={data.alertasStock} color="#C62828" />}
            <StatCard label="Total de pedidos" value={data.totalPedidos} color="#1565C0" />
            <StatCard label="Pedidos pendientes" value={data.pedidosPendientes} color="#E65100" />
            <StatCard label="En tránsito" value={data.pedidosEnTransito} color="#6A1B9A" />
            <StatCard label="Entregados" value={data.pedidosEntregados} color="#2E7D32" />
          </div>
        </>
      )}
      {!data && !error && <p className="text-muted">Cargando...</p>}
    </div>
  );
}
