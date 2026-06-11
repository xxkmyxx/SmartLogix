import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Navbar from './components/Navbar/Navbar';
import PrivateRoute from './components/PrivateRoute/PrivateRoute';
import Login from './pages/Login/Login';
import Dashboard from './pages/Dashboard/Dashboard';
import Inventario from './pages/Inventario/Inventario';
import Pedidos from './pages/Pedidos/Pedidos';
import Usuarios from './pages/Usuarios/Usuarios';
import Envios from './pages/Envios/Envios';

function App() {
  const { token } = useAuth();

  return (
    <>
      {token && <Navbar />}
      <Routes>
        <Route path="/login" element={token ? <Navigate to="/" /> : <Login />} />
        <Route path="/" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
        <Route path="/inventario" element={<PrivateRoute><Inventario /></PrivateRoute>} />
        <Route path="/pedidos" element={<PrivateRoute><Pedidos /></PrivateRoute>} />
        <Route path="/envios" element={<PrivateRoute><Envios /></PrivateRoute>} />
        <Route path="/usuarios" element={<PrivateRoute roles={['ADMIN']}><Usuarios /></PrivateRoute>} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </>
  );
}

export default App;
