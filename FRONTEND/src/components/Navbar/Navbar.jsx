import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
  const { user, logout } = useAuth();

  return (
    <nav className="navbar">
      <span className="navbar-brand">SmartLogix</span>
      <div className="navbar-links">
        <NavLink to="/" end>Dashboard</NavLink>
        {['ADMIN', 'OPERADOR'].includes(user?.role) && (
          <NavLink to="/inventario">Inventario</NavLink>
        )}
        <NavLink to="/pedidos">Pedidos</NavLink>
        <NavLink to="/envios">Envíos</NavLink>
        {user?.role === 'ADMIN' && (
          <NavLink to="/usuarios">Usuarios</NavLink>
        )}
      </div>
      <div className="navbar-user">
        <span>{user?.sub}</span>
        <span className="role-badge">{user?.role}</span>
        <button onClick={logout} className="btn-logout">Salir</button>
      </div>
    </nav>
  );
}
