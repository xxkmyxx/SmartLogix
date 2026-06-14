import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { usePortalAuth } from '../../context/PortalAuthContext';
import portalApi from '../../api/portalApi';
import './PortalLogin.css';

export default function PortalLogin() {
  const [form, setForm] = useState({ email: '', contrasena: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = usePortalAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await portalApi.post('/api/auth/login', form);
      if (data.rol !== 'CLIENTE') {
        setError('Esta cuenta no es de cliente. Usa el acceso interno.');
        return;
      }
      login(data.token, data.nombre);
      navigate('/portal/catalogo');
    } catch (err) {
      setError(err.response?.data?.error || 'Credenciales inválidas');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="portal-auth-page">
      <div className="portal-auth-card">
        <h2 className="portal-auth-title">Acceso al portal</h2>
        <p className="portal-auth-sub">Ingresa con tu cuenta de cliente</p>
        <form onSubmit={handleSubmit} className="portal-auth-form">
          <div className="portal-field">
            <label>Email</label>
            <input
              type="email"
              placeholder="correo@ejemplo.com"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
            />
          </div>
          <div className="portal-field">
            <label>Contraseña</label>
            <input
              type="password"
              placeholder="••••••••"
              value={form.contrasena}
              onChange={(e) => setForm({ ...form, contrasena: e.target.value })}
              required
            />
          </div>
          {error && <p className="portal-auth-error">{error}</p>}
          <button type="submit" className="portal-auth-btn" disabled={loading}>
            {loading ? 'Ingresando...' : 'Ingresar'}
          </button>
        </form>
        <p className="portal-auth-footer">
          ¿No tienes cuenta? <Link to="/portal/registro">Regístrate aquí</Link>
        </p>
        <p className="portal-auth-footer">
          <a href="/seguimiento">Rastrear un pedido →</a>
        </p>
      </div>
    </div>
  );
}
