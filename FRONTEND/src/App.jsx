import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { PortalAuthProvider } from './context/PortalAuthContext';
import Navbar from './components/Navbar/Navbar';
import PrivateRoute from './components/PrivateRoute/PrivateRoute';
import PortalPrivateRoute from './components/PortalPrivateRoute/PortalPrivateRoute';
import Login from './pages/Login/Login';
import Dashboard from './pages/Dashboard/Dashboard';
import Inventario from './pages/Inventario/Inventario';
import Pedidos from './pages/Pedidos/Pedidos';
import Usuarios from './pages/Usuarios/Usuarios';
import Envios from './pages/Envios/Envios';
import Seguimiento from './pages/Seguimiento/Seguimiento';
import PortalLayout from './pages/Portal/PortalLayout';
import PortalLogin from './pages/Portal/PortalLogin';
import PortalRegistro from './pages/Portal/PortalRegistro';
import PortalCatalogo from './pages/Portal/PortalCatalogo';
import PortalMisPedidos from './pages/Portal/PortalMisPedidos';

function App() {
  const { token } = useAuth();

  return (
    <>
      {token && <Navbar />}
      <Routes>
        <Route path="/seguimiento" element={<Seguimiento />} />
        <Route path="/login" element={token ? <Navigate to="/" /> : <Login />} />
        <Route path="/" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
        <Route path="/inventario" element={<PrivateRoute><Inventario /></PrivateRoute>} />
        <Route path="/pedidos" element={<PrivateRoute><Pedidos /></PrivateRoute>} />
        <Route path="/envios" element={<PrivateRoute><Envios /></PrivateRoute>} />
        <Route path="/usuarios" element={<PrivateRoute roles={['ADMIN']}><Usuarios /></PrivateRoute>} />

        <Route path="/portal" element={<PortalAuthProvider><PortalLayout /></PortalAuthProvider>}>
          <Route index element={<Navigate to="login" replace />} />
          <Route path="login" element={<PortalLogin />} />
          <Route path="registro" element={<PortalRegistro />} />
          <Route path="catalogo" element={<PortalPrivateRoute><PortalCatalogo /></PortalPrivateRoute>} />
          <Route path="mis-pedidos" element={<PortalPrivateRoute><PortalMisPedidos /></PortalPrivateRoute>} />
        </Route>

        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </>
  );
}

export default App;
