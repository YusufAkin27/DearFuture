import api from './axiosConfig';

export const sendCode = (email) => api.post('/auth/send-code', { email });
export const verifyCode = (email, code) => api.post('/auth/verify', { email, code });
