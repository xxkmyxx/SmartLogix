import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { usePortalAuth } from '../../context/PortalAuthContext';
import './PortalLayout.css';

export default function PortalLayout() {
  const { token, nombre, logout } = usePortalAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/portal/login');
  };

  return (
    <div className="portal-wrapper">
      <nav className="portal-nav">
        <span className="portal-nav-brand">SmartLogix <span className="portal-nav-badge">Portal</span></span>
        {token && (
          <div className="portal-nav-links">
            <NavLink to="/portal/catalogo" className={({ isActive }) => isActive ? 'portal-nav-link active' : 'portal-nav-link'}>
              Catálogo
            </NavLink>
            <NavLink to="/portal/mis-pedidos" className={({ isActive }) => isActive ? 'portal-nav-link active' : 'portal-nav-link'}>
              Mis pedidos
            </NavLink>
          </div>
        )}
        <div className="portal-nav-right">
          {token ? (
            <>
              <span className="portal-nav-user">{nombre}</span>
              <button className="portal-nav-logout" onClick={handleLogout}>Salir</button>
            </>
          ) : (
            <NavLink to="/portal/login" className="portal-nav-link">Iniciar sesión</NavLink>
          )}
        </div>
      </nav>
      <main className="portal-main">
        <Outlet />
      </main>
    </div>
  );
}
