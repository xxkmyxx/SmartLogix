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
  const isAdmin = user?.role === 'ADMIN';
  const canCreate = ['ADMIN', 'OPERADOR'].includes(user?.role);
  const canEdit = ['ADMIN', 'OPERADOR', 'TRANSPORTISTA'].includes(user?.role);

  const [envios, setEnvios] = useState([]);
  const [transportistas, setTransportistas] = useState([]);
  const [filtro, setFiltro] = useState('');
  const [cambiando, setCambiando] = useState(null);
  const [loading, setLoading] = useState(true);

  const [modal, setModal] = useState(false);
  const [modalTransportista, setModalTransportista] = useState(false);
  const [form, setForm] = useState({ pedidoId: '', transportistaId: '', fechaEstimada: '' });
  const [formT, setFormT] = useState({ nombre: '', telefono: '', patente: '' });
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState('');

  const cargar = async () => {
    try {
      const [enviosRes, transportistasRes] = await Promise.all([
        api.get('/api/envios'),
        api.get('/api/transportistas'),
      ]);
      setEnvios(enviosRes.data || []);
      setTransportistas(transportistasRes.data || []);
    } catch {
      setEnvios([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { cargar(); }, []);

  const lista = filtro ? envios.filter((e) => e.estado === filtro) : envios;

  const abrirModal = () => {
    setForm({ pedidoId: '', transportistaId: '', fechaEstimada: '' });
    setError('');
    setModal(true);
  };

  const guardarEnvio = async () => {
    setError('');
    if (!form.pedidoId || !form.transportistaId || !form.fechaEstimada) {
      setError('Todos los campos son obligatorios');
      return;
    }
    setGuardando(true);
    try {
      await api.post('/api/envios', {
        pedidoId: Number(form.pedidoId),
        transportistaId: Number(form.transportistaId),
        fechaEstimada: form.fechaEstimada,
      });
      setModal(false);
      cargar();
    } catch (err) {
      setError(err.response?.data?.error || 'Error al crear envío');
    } finally {
      setGuardando(false);
    }
  };

  const guardarTransportista = async () => {
    setError('');
    if (!formT.nombre || !formT.telefono || !formT.patente) {
      setError('Todos los campos son obligatorios');
      return;
    }
    setGuardando(true);
    try {
      await api.post('/api/transportistas', formT);
      setModalTransportista(false);
      cargar();
    } catch (err) {
      setError(err.response?.data?.error || 'Error al registrar transportista');
    } finally {
      setGuardando(false);
    }
  };

  const cambiarEstado = async (id, nuevoEstado) => {
    try {
      await api.put(`/api/envios/${id}/estado?estado=${nuevoEstado}`);
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
          {isAdmin && (
            <button className="btn-secondary" onClick={() => { setFormT({ nombre: '', telefono: '', patente: '' }); setError(''); setModalTransportista(true); }}>
              + Transportista
            </button>
          )}
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
              <th>Pedido ID</th>
              <th>Transportista</th>
              <th>Patente</th>
              <th>Estado</th>
              <th>Fecha Estimada</th>
              {canEdit && <th>Acciones</th>}
            </tr>
          </thead>
          <tbody>
            {lista.map((e) => (
              <tr key={e.id}>
                <td>{e.id}</td>
                <td>{e.pedidoId}</td>
                <td>{e.nombreTransportista || '—'}</td>
                <td>{e.patenteTransportista || '—'}</td>
                <td><span className={`badge ${badgeClass(e.estado)}`}>{e.estado}</span></td>
                <td>{e.fechaEstimada || '—'}</td>
                {canEdit && (
                  <td>
                    {!['ENTREGADO', 'CANCELADO'].includes(e.estado) && (
                      <div className="flex gap-2">
                        {cambiando === e.id ? (
                          <>
                            <select style={{ width: 'auto', padding: '4px 8px', fontSize: 12 }}
                              defaultValue={e.estado}
                              onChange={(ev) => cambiarEstado(e.id, ev.target.value)}>
                              {ESTADOS.filter((s) => !['ENTREGADO', 'CANCELADO'].includes(s)).map((s) => (
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
                <td colSpan={canEdit ? 7 : 6} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>
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
            <label>ID del Pedido *</label>
            <input type="number" value={form.pedidoId} onChange={(e) => setForm({ ...form, pedidoId: e.target.value })} placeholder="Ej: 1" />
            <label style={{ marginTop: 12 }}>Transportista *</label>
            <select value={form.transportistaId} onChange={(e) => setForm({ ...form, transportistaId: e.target.value })}>
              <option value="">Seleccionar...</option>
              {transportistas.map((t) => (
                <option key={t.id} value={t.id}>{t.nombre} — {t.patente}</option>
              ))}
            </select>
            <label style={{ marginTop: 12 }}>Fecha estimada de entrega *</label>
            <input type="date" value={form.fechaEstimada} onChange={(e) => setForm({ ...form, fechaEstimada: e.target.value })} />
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

      {modalTransportista && (
        <div className="modal-overlay" onClick={() => setModalTransportista(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 440 }}>
            <h2 style={{ marginBottom: 20 }}>Registrar Transportista</h2>
            <label>Nombre *</label>
            <input value={formT.nombre} onChange={(e) => setFormT({ ...formT, nombre: e.target.value })} placeholder="Nombre completo" />
            <label style={{ marginTop: 12 }}>Teléfono *</label>
            <input value={formT.telefono} onChange={(e) => setFormT({ ...formT, telefono: e.target.value })} placeholder="912345678" />
            <label style={{ marginTop: 12 }}>Patente *</label>
            <input value={formT.patente} onChange={(e) => setFormT({ ...formT, patente: e.target.value })} placeholder="ABC123" maxLength={10} />
            {error && <p style={{ color: 'var(--danger)', marginTop: 8 }}>{error}</p>}
            <div className="flex gap-2" style={{ marginTop: 20, justifyContent: 'flex-end' }}>
              <button className="btn-secondary" onClick={() => setModalTransportista(false)}>Cancelar</button>
              <button className="btn-primary" onClick={guardarTransportista} disabled={guardando}>
                {guardando ? 'Guardando...' : 'Registrar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
