import { useEffect, useState } from 'react';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';

const ESTADOS = ['PENDIENTE', 'RECOGIDO', 'EN_TRANSITO', 'ENTREGADO', 'CANCELADO'];

const badgeClass = (estado) => ({
  PENDIENTE: 'badge-orange',
  RECOGIDO: 'badge-blue',
  EN_TRANSITO: 'badge-blue',
  ENTREGADO: 'badge-green',
  CANCELADO: 'badge-red',
}[estado] || 'badge-gray');

export default function Envios() {
  const { user } = useAuth();
  const canCreate = ['ADMIN', 'OPERADOR'].includes(user?.role);
  const canEdit = user?.role === 'TRANSPORTISTA';

  const [envios, setEnvios] = useState([]);
  const [transportistas, setTransportistas] = useState([]);
  const [pedidos, setPedidos] = useState([]);
  const [filtro, setFiltro] = useState('');
  const [cambiando, setCambiando] = useState(null);
  const [loading, setLoading] = useState(true);

  const [modal, setModal] = useState(false);
  const [form, setForm] = useState({ pedidoId: '', transportistaId: '', fechaEstimada: '' });
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState('');

  const cargar = async () => {
    try {
      const url = user?.role === 'TRANSPORTISTA'
        ? `/api/envios/transportista/${user.userId}`
        : '/api/envios';
      const res = await api.get(url);
      setEnvios(res.data || []);
    } catch {
      setEnvios([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { cargar(); }, []);

  const lista = filtro ? envios.filter((e) => e.estado === filtro) : envios;

  const abrirModal = async () => {
    setForm({ pedidoId: '', transportistaId: '', fechaEstimada: '' });
    setError('');
    try {
      const [resUsuarios, resPedidos] = await Promise.all([
        api.get('/api/usuarios'),
        api.get('/api/pedidos'),
      ]);
      setTransportistas((resUsuarios.data || []).filter((u) => u.rol === 'TRANSPORTISTA' && u.activo));
      const pedidosDisponibles = (resPedidos.data || []).filter(
        (p) => p.estado === 'PENDIENTE'
      );
      setPedidos(pedidosDisponibles);
    } catch {
      setTransportistas([]);
      setPedidos([]);
    }
    setModal(true);
  };

  const guardarEnvio = async () => {
    setError('');
    if (!form.pedidoId || !form.transportistaId || !form.fechaEstimada) {
      setError('Todos los campos son obligatorios');
      return;
    }
    setGuardando(true);
    const transportistaSeleccionado = transportistas.find((t) => String(t.id) === String(form.transportistaId));
    const pedidoSeleccionado = pedidos.find((p) => String(p.id) === String(form.pedidoId));
    try {
      await api.post('/api/envios', {
        pedidoId: Number(form.pedidoId),
        numeroPedido: pedidoSeleccionado?.numeroPedido || '',
        transportistaId: Number(form.transportistaId),
        nombreTransportista: transportistaSeleccionado?.nombre || '',
        fechaEstimada: form.fechaEstimada,
      });
      setModal(false);
      cargar();
    } catch (err) {
      setError(err.response?.data?.error || err.response?.data?.message || err.response?.data?.detail || 'Error al crear envío');
    } finally {
      setGuardando(false);
    }
  };

  const cambiarEstado = async (id, nuevoEstado) => {
    const envio = envios.find((e) => e.id === id);
    try {
      await api.put(`/api/envios/${id}/estado?estado=${nuevoEstado}`);
      if (nuevoEstado === 'ENTREGADO' && envio?.pedidoId) {
        await api.put(`/api/pedidos/${envio.pedidoId}/estado?nuevoEstado=ENTREGADO`);
      }
      setCambiando(null);
      cargar();
    } catch (err) {
      alert(err.response?.data?.error || 'Error al cambiar estado');
    }
  };

  if (loading) return <div className="page"><p>Cargando...</p></div>;

  return (
    <div className="page">
      <div className="flex-between" style={{ marginBottom: 20 }}>
        <h1 className="page-title" style={{ margin: 0 }}>Envíos</h1>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <select style={{ width: 'auto' }} value={filtro} onChange={(e) => setFiltro(e.target.value)}>
            <option value="">Todos los estados</option>
            {ESTADOS.map((e) => <option key={e} value={e}>{e}</option>)}
          </select>
          {canCreate && (
            <button className="btn-primary" onClick={abrirModal}>+ Nuevo Envío</button>
          )}
        </div>
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Pedido</th>
              <th>Transportista</th>
              <th>Estado</th>
              <th>Fecha Estimada</th>
              {canEdit && <th>Acciones</th>}
            </tr>
          </thead>
          <tbody>
            {lista.map((e) => (
              <tr key={e.id}>
                <td>{e.id}</td>
                <td>{e.numeroPedido || `PED-${String(e.pedidoId).padStart(5, '0')}`}</td>
                <td>{e.nombreTransportista || '—'}</td>
                <td><span className={`badge ${badgeClass(e.estado)}`}>{e.estado}</span></td>
                <td>{e.fechaEstimada || '—'}</td>
                {canEdit && (
                  <td>
                    {!['ENTREGADO', 'CANCELADO'].includes(e.estado) && (
                      <div className="flex gap-2">
                        {cambiando === e.id ? (
                          <>
                            <select
                              style={{ width: 'auto', padding: '4px 8px', fontSize: 12 }}
                              defaultValue={e.estado}
                              onChange={(ev) => cambiarEstado(e.id, ev.target.value)}
                            >
                              {ESTADOS.map((s) => (
                                <option key={s} value={s}>{s}</option>
                              ))}
                            </select>
                            <button className="btn-secondary btn-sm" onClick={() => setCambiando(null)}>✕</button>
                          </>
                        ) : (
                          <button className="btn-secondary btn-sm" onClick={() => setCambiando(e.id)}>Estado</button>
                        )}
                      </div>
                    )}
                  </td>
                )}
              </tr>
            ))}
            {lista.length === 0 && (
              <tr>
                <td colSpan={canEdit ? 6 : 5} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>
                  Sin envíos
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {modal && (
        <div className="modal-overlay" onClick={() => setModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 480 }}>
            <h2 style={{ marginBottom: 20 }}>Nuevo Envío</h2>
            <label>Pedido *</label>
            <select value={form.pedidoId} onChange={(e) => setForm({ ...form, pedidoId: e.target.value })}>
              <option value="">Seleccionar pedido...</option>
              {pedidos.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.numeroPedido} — {p.clienteNombre || p.clienteEmail} ({p.estado})
                </option>
              ))}
            </select>
            {pedidos.length === 0 && (
              <p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4 }}>
                No hay pedidos disponibles.
              </p>
            )}
            <label style={{ marginTop: 12 }}>Transportista *</label>
            <select value={form.transportistaId} onChange={(e) => setForm({ ...form, transportistaId: e.target.value })}>
              <option value="">Seleccionar...</option>
              {transportistas.map((t) => (
                <option key={t.id} value={t.id}>{t.nombre} — {t.email}</option>
              ))}
            </select>
            {transportistas.length === 0 && (
              <p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4 }}>
                No hay transportistas activos. Créalos en la sección Usuarios.
              </p>
            )}
            <label style={{ marginTop: 12 }}>Fecha estimada de entrega *</label>
            <input
              type="date"
              value={form.fechaEstimada}
              min={new Date().toISOString().split('T')[0]}
              onChange={(e) => setForm({ ...form, fechaEstimada: e.target.value })}
            />
            {error && <p style={{ color: 'var(--danger)', marginTop: 8 }}>{error}</p>}
            <div className="flex gap-2" style={{ marginTop: 20, justifyContent: 'flex-end' }}>
              <button className="btn-secondary" onClick={() => setModal(false)}>Cancelar</button>
              <button className="btn-primary" onClick={guardarEnvio} disabled={guardando}>
                {guardando ? 'Guardando...' : 'Guardar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
