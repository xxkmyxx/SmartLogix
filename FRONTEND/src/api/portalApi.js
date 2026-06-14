import axios from 'axios';

const portalApi = axios.create({
  baseURL: 'http://localhost:8084',
  headers: { 'Content-Type': 'application/json' },
});

portalApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('portal_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

portalApi.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('portal_token');
      localStorage.removeItem('portal_nombre');
      window.location.href = '/portal/login';
    }
    return Promise.reject(err);
  }
);

export default portalApi;
