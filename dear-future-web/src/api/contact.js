import api from './axiosConfig';

/**
 * İletişim mesajı gönderir. E-posta doğrulama kodu gönderilir.
 * @param {{ name: string, email: string, subject: string, message: string, phone?: string }}
 */
export const sendContactMessage = (data) => api.post('/contact/send', data);

/**
 * E-posta doğrulama kodu ile mesajı doğrular.
 * @param {string} code - 6 haneli kod
 */
export const verifyContactEmail = (code) => api.post('/contact/verify-email', { code });
