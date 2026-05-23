import { useEffect, useState } from 'react';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import './Inventario.css';

const EMPTY_FORM = { nombre: '', descripcion: '', categoria: '', precio: '', stockInicial: '', stockMinimo: '' };

export default function Inventario() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [productos, setProductos] = useState([]);
  const [alertas, setAlertas] = useState([]);
  const [modal, setModal] = useState(false);
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState('');

  const cargar = () => {
    api.get('/api/inventario/productos').then((r) => setProductos(r.data));
    api.get('/api/inventario/alertas').then((r) => setAlertas(r.data));
  };

  useEffect(() => { cargar(); }, []);

  const abrirModal = (p = null) => {
    setEditando(p);
    setForm(p ? { nombre: p.nombre, descripcion: p.descripcion, categoria: p.categoria, precio: p.precio, stockActual: p.stockActual, stockMinimo: p.stockMinimo } : EMPTY_FORM);
    setError('');
    setModal(true);
  };

  const guardar = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (editando) {
        await api.put(`/api/inventario/productos/${editando.id}`, form);
      } else {
        await api.post('/api/inventario/productos', form);
      }
      setModal(false);
      cargar();
    } catch (err) {
      setError(err.response?.data?.error || 'Error al guardar');
    }
  };

  const eliminar = async (id) => {
    if (!window.confirm('¿Eliminar producto?')) return;
    try {
      await api.delete(`/api/inventario/productos/${id}`);
      cargar();
    } catch (err) {
      alert(err.response?.data?.error || 'Error al eliminar');
    }
  };

  return (
    <div className="page">
      <div className="flex-between" style={{ marginBottom: 20 }}>
        <h1 className="page-title" style={{ margin: 0 }}>Inventario</h1>
        {isAdmin && (
          <button className="btn-primary" onClick={() => abrirModal()}>+ Nuevo producto</button>
        )}
      </div>

      {alertas.length > 0 && (
        <div className="alerta-banner">
          ⚠️ {alertas.length} producto(s) con stock bajo
        </div>
      )}

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>ID</th><th>Nombre</th><th>Categoría</th><th>Precio</th><th>Stock</th>
              {isAdmin && <th>Acciones</th>}
            </tr>
          </thead>
          <tbody>
            {productos.map((p) => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>{p.nombre}</td>
                <td>{p.categoria}</td>
                <td>${Number(p.precio).toLocaleString('es-CL')}</td>
                <td>
                  <span className={`badge ${p.stockActual <= (p.stockMinimo || 0) ? 'badge-red' : 'badge-green'}`}>
                    {p.stockActual}
                  </span>
                </td>
                {isAdmin && (
                  <td className="flex gap-2">
                    <button className="btn-secondary btn-sm" onClick={() => abrirModal(p)}>Editar</button>
                    <button className="btn-danger btn-sm" onClick={() => eliminar(p.id)}>Eliminar</button>
                  </td>
                )}
              </tr>
            ))}
            {productos.length === 0 && (
              <tr><td colSpan={isAdmin ? 6 : 5} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Sin productos</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {modal && (
        <div className="modal-overlay" onClick={() => setModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">{editando ? 'Editar producto' : 'Nuevo producto'}</h2>
            <form onSubmit={guardar}>
              <div className="form-group">
                <label>Nombre</label>
                <input value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} maxLength={80} required />
              </div>
              <div className="form-group">
                <label>Descripcion</label>
                <input value={form.descripcion} onChange={(e) => setForm({ ...form, descripcion: e.target.value })} maxLength={200} />
              </div>
              <div className="form-group">
                <label>Categoria</label>
                <input value={form.categoria} onChange={(e) => setForm({ ...form, categoria: e.target.value })} maxLength={40} />
              </div>
              <div className="form-group">
                <label>Precio</label>
                <input type="number" min="0" step="0.01" value={form.precio} onChange={(e) => setForm({ ...form, precio: e.target.value })} required />
              </div>
              {!editando && (
                <div className="form-group">
                  <label>Stock inicial</label>
                  <input type="number" min="0" value={form.stockInicial} onChange={(e) => setForm({ ...form, stockInicial: e.target.value })} />
                </div>
              )}
              {editando && (
                <div className="form-group">
                  <label>Stock actual</label>
                  <input type="number" min="0" value={form.stockActual} onChange={(e) => setForm({ ...form, stockActual: e.target.value })} />
                </div>
              )}
              <div className="form-group">
                <label>Stock mínimo</label>
                <input type="number" min="0" value={form.stockMinimo} onChange={(e) => setForm({ ...form, stockMinimo: e.target.value })} />
              </div>
              {error && <p className="error-msg">{error}</p>}
              <div className="flex gap-2" style={{ justifyContent: 'flex-end' }}>
                <button type="button" className="btn-secondary" onClick={() => setModal(false)}>Cancelar</button>
                <button type="submit" className="btn-primary">Guardar</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
