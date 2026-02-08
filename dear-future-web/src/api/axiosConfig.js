import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add a request interceptor to include the JWT token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Oturum gerektiren hatalarda token temizle ve login'e yönlendir
function redirectToLogin() {
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
