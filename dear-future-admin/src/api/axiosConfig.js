import axios from 'axios';

const baseURL = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '') || '';
const apiBase = baseURL ? `${baseURL}/api` : '/api';

export const api = axios.create({
  baseURL: apiBase,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('adminToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response?.status === 401 || err.response?.status === 403) {
      localStorage.removeItem('adminToken');
      if (!window.location.pathname.includes('/login')) {
        window.location.replace('/login');
      }
    }
    return Promise.reject(err);
  }
);

export default api;
