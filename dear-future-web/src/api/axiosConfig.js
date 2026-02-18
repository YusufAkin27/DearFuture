import axios from 'axios';
import { getCacheConfig, cacheKey, get, set, clear } from './cache';

const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

const defaultAdapter = api.defaults.adapter;

// Add a request interceptor to include the JWT token and optionally use cache
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        const cacheConfig = config.method === 'get' && getCacheConfig(config.url);
        if (cacheConfig) {
            const key = cacheKey(config);
            const cached = get(key);
            if (cached) {
                config.adapter = () => Promise.resolve(cached);
                return config;
            }
            config.__cacheKey = key;
            config.__cacheTtl = cacheConfig.ttl;
            config.adapter = (cfg) =>
                defaultAdapter(cfg).then((response) => {
                    if (cfg.__cacheKey) set(cfg.__cacheKey, response, cfg.__cacheTtl);
                    return response;
                });
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Oturum gerektiren hatalarda token temizle, cache'i temizle ve login'e yönlendir
function redirectToLogin() {
    clear();
    localStorage.removeItem('token');
    window.location.replace('/login');
}

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response?.status;
        if (status === 401 || status === 403) {
            redirectToLogin();
            return Promise.reject(error);
        }
        return Promise.reject(error);
    }
);

export default api;
