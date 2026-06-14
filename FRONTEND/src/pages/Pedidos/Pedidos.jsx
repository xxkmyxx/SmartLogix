import { useEffect, useState } from 'react';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';

const ESTADOS = ['PENDIENTE', 'CONFIRMADO', 'EN_PREPARACION', 'EN_TRANSITO', 'ENTREGADO', 'CANCELADO'];

const badgeClass = (estado) => ({
  PENDIENTE: 'badge-orange',
  CONFIRMADO: 'badge-blue',
  EN_PREPARACION: 'badge-blue',
  EN_TRANSITO: 'badge-blue',
  ENTREGADO: 'badge-green',
  CANCELADO: 'badge-red',
}[estado] || 'badge-gray');

const detalleVacio = () => ({ productoId: '', productoCodigo: '', productoNombre: '', cantidad: 1, precioUnitario: '' });

export default function Pedidos() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const canEdit = ['ADMIN', 'OPERADOR'].includes(user?.role);
  const canCreate = ['ADMIN', 'OPERADOR'].includes(user?.role);

  const [pedidos, setPedidos] = useState([]);
  const [filtro, setFiltro] = useState('');
  const [cambiando, setCambiando] = useState(null);

  const [modal, setModal] = useState(false);
  const [productos, setProductos] = useState([]);
  const [form, setForm] = useState({ clienteEmail: '', clienteNombre: '', observaciones: '', detalles: [detalleVacio()] });
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState('');

  const cargar = () => api.get('/api/pedidos').then((r) => setPedidos(r.data));

  useEffect(() => { cargar(); }, []);

  const abrirModal = async () => {
    setError('');
    setForm({ clienteEmail: '', clienteNombre: '', observaciones: '', detalles: [detalleVacio()] });
    try {
      const r = await api.get('/api/inventario/productos');
      setProductos(r.data || []);
    } catch {
      setProductos([]);
    }
    setModal(true);
  };

  const cerrarModal = () => { setModal(false); setError(''); };

  const cambiarDetalle = (i, campo, valor) => {
    setForm((prev) => {
      const detalles = [...prev.detalles];
      if (campo === 'productoId') {
        const prod = productos.find((p) => String(p.id) === valor);
        detalles[i] = {
          ...detalles[i],
          productoId: prod ? prod.id : '',
          productoCodigo: prod ? (prod.codigo || prod.sku || '') : '',
          productoNombre: prod ? prod.nombre : '',
          precioUnitario: prod ? (prod.precio || '') : '',
        };
      } else {
        detalles[i] = { ...detalles[i], [campo]: valor };
      }
      return { ...prev, detalles };
    });
  };

  const agregarLinea = () => setForm((prev) => ({ ...prev, detalles: [...prev.detalles, detalleVacio()] }));
  const quitarLinea = (i) => setForm((prev) => ({ ...prev, detalles: prev.detalles.filter((_, idx) => idx !== i) }));

  const guardar = async () => {
    setError('');
    if (!form.clienteEmail || !form.clienteNombre) { setError('Email y nombre del cliente son obligatorios'); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.clienteEmail)) { setError('Ingresa un email válido'); return; }
    if (form.detalles.length === 0) { setError('Agrega al menos un producto'); return; }
    for (const d of form.detalles) {
      if (!d.productoId || !d.cantidad || !d.precioUnitario) { setError('Completa todos los campos de cada línea de producto'); return; }
    }
    setGuardando(true);
    try {
      await api.post('/api/pedidos', {
        clienteEmail: form.clienteEmail,
        clienteNombre: form.clienteNombre,
        observaciones: form.observaciones,
        detalles: form.detalles.map((d) => ({
          productoId: Number(d.productoId),
          productoCodigo: d.productoCodigo,
          productoNombre: d.productoNombre,
          cantidad: Number(d.cantidad),
          precioUnitario: Number(d.precioUnitario),
        })),
      });
      cerrarModal();
      cargar();
    } catch (err) {
      setError(err.response?.data?.error || 'Error al crear pedido');
    } finally {
      setGuardando(false);
    }
  };

  const cambiarEstado = async (id, nuevoEstado) => {
    try {
      await api.put(`/api/pedidos/${id}/estado?nuevoEstado=${nuevoEstado}`);
      setCambiando(null);
      cargar();
    } catch (err) {
      alert(err.response?.data?.error || 'Error al cambiar estado');
    }
  };

  const cancelar = async (id) => {
    if (!window.confirm('¿Cancelar este pedido?')) return;
    try {
      await api.delete(`/api/pedidos/${id}`);
      cargar();
    } catch (err) {
      alert(err.response?.data?.error || 'Error al cancelar');
    }
  };

  const lista = filtro ? pedidos.filter((p) => p.estado === filtro) : pedidos;

  return (
    <div className="page">
      <div className="flex-between" style={{ marginBottom: 20 }}>
        <h1 className="page-title" style={{ margin: 0 }}>Pedidos</h1>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <select style={{ width: 'auto' }} value={filtro} onChange={(e) => setFiltro(e.target.value)}>
            <option value="">Todos los estados</option>
            {ESTADOS.map((e) => <option key={e} value={e}>{e}</option>)}
          </select>
          {canCreate && (
            <button className="btn-primary" onClick={abrirModal}>+ Nuevo Pedido</button>
          )}
        </div>
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>Número</th><th>Cliente</th><th>Estado</th><th>Fecha</th>
              {canEdit && <th>Acciones</th>}
            </tr>
          </thead>
          <tbody>
            {lista.map((p) => (
              <tr key={p.id}>
                <td>{p.numeroPedido}</td>
                <td>{p.clienteEmail}</td>
                <td><span className={`badge ${badgeClass(p.estado)}`}>{p.estado}</span></td>
                <td>{p.fechaCreacion ? new Date(p.fechaCreacion).toLocaleDateString('es-CL') : '—'}</td>
                {canEdit && (
                  <td>
                    {!['ENTREGADO', 'CANCELADO'].includes(p.estado) && (
                      <div className="flex gap-2">
                        {cambiando === p.id ? (
                          <>
                            <select style={{ width: 'auto', padding: '4px 8px', fontSize: 12 }}
                              defaultValue={p.estado}
                              onChange={(e) => cambiarEstado(p.id, e.target.value)}>
                              {ESTADOS.filter((e) => !['ENTREGADO', 'CANCELADO'].includes(e)).map((e) => <option key={e} value={e}>{e}</option>)}
                            </select>
                            <button className="btn-secondary btn-sm" onClick={() => setCambiando(null)}>✕</button>
                          </>
                        ) : (
                          <>
                            <button className="btn-secondary btn-sm" onClick={() => setCambiando(p.id)}>Estado</button>
                            {isAdmin && (
                              <button className="btn-danger btn-sm" onClick={() => cancelar(p.id)}>Cancelar</button>
                            )}
                          </>
                        )}
                      </div>
                    )}
                  </td>
                )}
              </tr>
            ))}
            {lista.length === 0 && (
              <tr><td colSpan={canEdit ? 5 : 4} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Sin pedidos</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {modal && (
        <div className="modal-overlay" onClick={cerrarModal}>
          <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 680, width: '95%' }}>
            <h2 style={{ marginBottom: 20 }}>Nuevo Pedido</h2>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 12 }}>
              <div>
                <label>Email cliente *</label>
                <input value={form.clienteEmail} onChange={(e) => setForm({ ...form, clienteEmail: e.target.value })} placeholder="cliente@email.com" maxLength={80} />
              </div>
              <div>
                <label>Nombre cliente *</label>
                <input value={form.clienteNombre} onChange={(e) => setForm({ ...form, clienteNombre: e.target.value })} placeholder="Juan Pérez" maxLength={50} />
              </div>
            </div>

            <div style={{ marginBottom: 16 }}>
              <label>Observaciones</label>
              <input value={form.observaciones} onChange={(e) => setForm({ ...form, observaciones: e.target.value })} placeholder="Notas opcionales" maxLength={200} />
            </div>

            <div style={{ marginBottom: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <strong>Productos</strong>
              <button className="btn-secondary btn-sm" onClick={agregarLinea}>+ Línea</button>
            </div>

            {form.detalles.map((d, i) => (
              <div key={i} style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr auto', gap: 8, marginBottom: 8, alignItems: 'end' }}>
                <div>
                  {i === 0 && <label>Producto *</label>}
                  <select value={d.productoId} onChange={(e) => cambiarDetalle(i, 'productoId', e.target.value)}>
                    <option value="">Seleccionar...</option>
                    {productos.map((p) => (
                      <option key={p.id} value={p.id}>{p.nombre}</option>
                    ))}
                  </select>
                </div>
                <div>
                  {i === 0 && <label>Cantidad *</label>}
                  <input type="number" min="1" value={d.cantidad} onChange={(e) => cambiarDetalle(i, 'cantidad', e.target.value)} />
                </div>
                <div>
                  {i === 0 && <label>Precio unit. *</label>}
                  <input type="number" min="0" step="0.01" value={d.precioUnitario} onChange={(e) => cambiarDetalle(i, 'precioUnitario', e.target.value)} placeholder="0.00" />
                </div>
                <button className="btn-danger btn-sm" onClick={() => quitarLinea(i)} disabled={form.detalles.length === 1} style={{ marginBottom: 0 }}>✕</button>
              </div>
            ))}

            {error && <p style={{ color: 'var(--danger)', marginTop: 8 }}>{error}</p>}

            <div className="flex gap-2" style={{ marginTop: 20, justifyContent: 'flex-end' }}>
              <button className="btn-secondary" onClick={cerrarModal}>Cancelar</button>
              <button className="btn-primary" onClick={guardar} disabled={guardando}>
                {guardando ? 'Guardando...' : 'Guardar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
