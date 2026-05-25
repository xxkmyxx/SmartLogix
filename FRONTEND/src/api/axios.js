import axios from 'axios';

// Instancia base de axios con la URL del backend
const api = axios.create({
  baseURL: 'http://localhost:8084',
  headers: { 'Content-Type': 'application/json' },
});

// Antes de cada request, adjunta el token del usuario si existe
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Si el servidor responde con 401, limpia el storage y redirige al login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default api;
