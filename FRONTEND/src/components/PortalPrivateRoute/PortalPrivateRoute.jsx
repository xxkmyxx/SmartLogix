import { Navigate } from 'react-router-dom';
import { usePortalAuth } from '../../context/PortalAuthContext';

export default function PortalPrivateRoute({ children }) {
  const { token } = usePortalAuth();
  return token ? children : <Navigate to="/portal/login" replace />;
}
