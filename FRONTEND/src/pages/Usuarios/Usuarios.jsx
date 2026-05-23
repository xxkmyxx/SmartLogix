import { useEffect, useState } from 'react';
import api from '../../api/axios';

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [modal, setModal] = useState(false);
  const [form, setForm] = useState({ nombre: '', email: '', contrasena: '', rol: 'OPERADOR' });
  const [error, setError] = useState('');

  const cargar = () => api.get('/api/usuarios').then((r) => setUsuarios(r.data));

  useEffect(() => { cargar(); }, []);

  const registrar = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.email.endsWith('@smartlogix.cl')) { setError('El email debe ser @smartlogix.cl'); return; }
    try {
      await api.post('/api/auth/register', form);
      setModal(false);
      setForm({ nombre: '', email: '', contrasena: '', rol: 'OPERADOR' });
      cargar();
    } catch (err) {
      setError(err.response?.data?.error || 'Error al registrar usuario');
    }
  };

  const toggleActivo = async (u) => {
    try {
      await api.put(`/api/usuarios/${u.id}`, { ...u, activo: !u.activo });
      cargar();
    } catch (err) {
      alert(err.response?.data?.error || 'Error');
    }
  };

  return (
    <div className="page">
      <div className="flex-between" style={{ marginBottom: 20 }}>
        <h1 className="page-title" style={{ margin: 0 }}>Usuarios</h1>
        <button className="btn-primary" onClick={() => setModal(true)}>+ Nuevo usuario</button>
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr><th>Nombre</th><th>Email</th><th>Rol</th><th>Estado</th><th>Acciones</th></tr>
          </thead>
          <tbody>
            {usuarios.map((u) => (
              <tr key={u.id}>
                <td>{u.nombre}</td>
                <td>{u.email}</td>
                <td><span className="badge badge-orange">{u.rol}</span></td>
                <td>
                  <span className={`badge ${u.activo ? 'badge-green' : 'badge-red'}`}>
                    {u.activo ? 'Activo' : 'Inactivo'}
                  </span>
                </td>
                <td>
                  <button className={`btn-sm ${u.activo ? 'btn-danger' : 'btn-secondary'}`} onClick={() => toggleActivo(u)}>
                    {u.activo ? 'Desactivar' : 'Activar'}
                  </button>
                </td>
              </tr>
            ))}
            {usuarios.length === 0 && (
              <tr><td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Sin usuarios</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {modal && (
        <div className="modal-overlay" onClick={() => setModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">Nuevo usuario</h2>
            <form onSubmit={registrar}>
              <div className="form-group">
                <label>Nombre</label>
                <input value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} maxLength={50} required />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} maxLength={80} placeholder="nombre@smartlogix.cl" required />
              </div>
              <div className="form-group">
                <label>Contraseña</label>
                <input type="password" value={form.contrasena} onChange={(e) => setForm({ ...form, contrasena: e.target.value })} maxLength={72} required />
              </div>
              <div className="form-group">
                <label>Rol</label>
                <select value={form.rol} onChange={(e) => setForm({ ...form, rol: e.target.value })}>
                  <option value="ADMIN">ADMIN</option>
                  <option value="OPERADOR">OPERADOR</option>
                  <option value="TRANSPORTISTA">TRANSPORTISTA</option>
                </select>
              </div>
              {error && <p className="error-msg">{error}</p>}
              <div className="flex gap-2" style={{ justifyContent: 'flex-end' }}>
                <button type="button" className="btn-secondary" onClick={() => setModal(false)}>Cancelar</button>
                <button type="submit" className="btn-primary">Registrar</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
