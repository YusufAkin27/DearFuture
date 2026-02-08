import api from './axiosConfig';
// Authentication API calls
export const login = async (email) => {
    return api.post('/auth/send-code', { email });
};

export const verifyCode = async (email, code) => {
    return api.post('/auth/verify', { email, code });
};

export const resendCode = async (email) => {
    return api.post('/auth/resend-code', { email });
};
