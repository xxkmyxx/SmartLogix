import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/axios';
import './Login.css';

export default function Login() {
  const [form, setForm] = useState({ email: '', contrasena: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await api.post('/api/auth/login', form);
      if (data.rol === 'CLIENTE') {
        setError('Acceso denegado. Los clientes deben ingresar por el portal.');
        return;
      }
      login(data.token);
      navigate('/');
    } catch (err) {
      const msg = err.response?.data?.mensaje || err.response?.data?.error || 'Credenciales inválidas';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-bg">
      <div className="login-card">
        <div className="login-logo">🥐</div>
        <h1 className="login-title">SmartLogix</h1>
        <p className="login-sub">Gestión logística para tu negocio</p>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
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
          <div className="form-group">
            <label>Contraseña</label>
            <input
              type="password"
              placeholder="••••••••"
              value={form.contrasena}
              onChange={(e) => setForm({ ...form, contrasena: e.target.value })}
              maxLength={72}
              required
            />
          </div>
          {error && <p className="error-msg">{error}</p>}
          <button type="submit" className="btn-primary login-btn" disabled={loading}>
            {loading ? 'Ingresando...' : 'Ingresar'}
          </button>
        </form>
        <a href="/seguimiento" className="seguimiento-link">
          ¿Quieres rastrear tu pedido? Haz clic aquí
        </a>
        <a href="/portal" className="seguimiento-link" style={{ marginTop: 6 }}>
          ¿Eres cliente? Accede al portal →
        </a>
      </div>
    </div>
  );
}
