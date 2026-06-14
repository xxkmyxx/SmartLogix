import { createContext, useContext, useState } from 'react';

const PortalAuthContext = createContext(null);

const parseJwt = (token) => {
  try { return JSON.parse(atob(token.split('.')[1])); } catch { return null; }
};

export function PortalAuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('portal_token'));
  const [nombre, setNombre] = useState(() => localStorage.getItem('portal_nombre') || '');

  const user = token ? parseJwt(token) : null;

  const login = (jwt, nombreUsuario) => {
    localStorage.setItem('portal_token', jwt);
    localStorage.setItem('portal_nombre', nombreUsuario || '');
    setToken(jwt);
    setNombre(nombreUsuario || '');
  };

  const logout = () => {
    localStorage.removeItem('portal_token');
    localStorage.removeItem('portal_nombre');
    setToken(null);
    setNombre('');
  };

  return (
    <PortalAuthContext.Provider value={{ token, user, nombre, login, logout }}>
      {children}
    </PortalAuthContext.Provider>
  );
}

export const usePortalAuth = () => useContext(PortalAuthContext);
