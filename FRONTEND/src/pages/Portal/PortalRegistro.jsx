import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import portalApi from '../../api/portalApi';
import './PortalLogin.css';

export default function PortalRegistro() {
  const [form, setForm] = useState({ nombre: '', email: '', contrasena: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await portalApi.post('/api/auth/register-cliente', form);
      navigate('/portal/login');
    } catch (err) {
      setError(err.response?.data?.error || 'Error al registrarse. Intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="portal-auth-page">
      <div className="portal-auth-card">
        <h2 className="portal-auth-title">Crear cuenta</h2>
        <p className="portal-auth-sub">Regístrate para hacer pedidos en línea</p>
        <form onSubmit={handleSubmit} className="portal-auth-form">
          <div className="portal-field">
            <label>Nombre completo</label>
            <input
              type="text"
              placeholder="Tu nombre"
              value={form.nombre}
              onChange={(e) => {
                const val = e.target.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s'-]/g, '');
                setForm({ ...form, nombre: val });
              }}
              maxLength={50}
              required
            />
          </div>
          <div className="portal-field">
            <label>Email</label>
            <input
              type="email"
              placeholder="correo@ejemplo.com"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              maxLength={80}
              required
            />
          </div>
          <div className="portal-field">
            <label>Contraseña</label>
            <input
              type="password"
              placeholder="Mínimo 6 caracteres"
              value={form.contrasena}
              onChange={(e) => setForm({ ...form, contrasena: e.target.value })}
              minLength={6}
              maxLength={72}
              required
            />
          </div>
          {error && <p className="portal-auth-error">{error}</p>}
          <button type="submit" className="portal-auth-btn" disabled={loading}>
            {loading ? 'Registrando...' : 'Crear cuenta'}
          </button>
        </form>
        <p className="portal-auth-footer">
          ¿Ya tienes cuenta? <Link to="/portal/login">Inicia sesión</Link>
        </p>
      </div>
    </div>
  );
}
