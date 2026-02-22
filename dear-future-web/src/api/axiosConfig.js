import axios from 'axios';
import { getCacheConfig, cacheKey, get, set, clear } from './cache';

// Frontend: https://dearfuture.com.tr — Backend: https://api.dearfuture.info (VITE_BACKEND_URL ile override)
const backendBase = (import.meta.env.VITE_BACKEND_URL || 'https://api.dearfuture.info').replace(/\/$/, '');
const apiBaseURL = `${backendBase}/api`;

const api = axios.create({
    baseURL: apiBaseURL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Hata ayıklama: istek ve hata loglama
const DEBUG = import.meta.env.DEV;

// Add a request interceptor: token ekle, cache hit ise önbellekten dön
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        if (DEBUG) {
            const url = (config.baseURL || '') + (config.url || '');
            console.log('[API] İstek:', config.method?.toUpperCase(), url, token ? '(token var)' : '(token yok)');
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
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// 401/403: token temizle, cache temizle, her zaman mevcut sitede (frontend) welcome sayfasına git — api.dearfuture.info/login'e düşmesin
function redirectToWelcome() {
    clear();
    localStorage.removeItem('token');
    const base = typeof window !== 'undefined' ? window.location.origin : '';
    window.location.replace(base ? `${base}/welcome` : '/welcome');
}

api.interceptors.response.use(
    (response) => {
        if (response.config?.__cacheKey && response.config?.__cacheTtl) {
            set(response.config.__cacheKey, response, response.config.__cacheTtl);
        }
        return response;
    },
    (error) => {
        const status = error.response?.status;
        const url = error.config?.baseURL + error.config?.url;
        const msg = error.response?.data?.message || error.message;
        if (DEBUG) {
            console.error('[API] Hata:', error.config?.method?.toUpperCase(), url, '→', status, msg || error.response?.data);
        }
        if (status === 401 || status === 403) {
            redirectToWelcome();
            return Promise.reject(error);
        }
        return Promise.reject(error);
    }
);

export default api;
