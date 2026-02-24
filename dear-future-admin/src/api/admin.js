import api from './axiosConfig';

export const getDashboard = () => api.get('/admin/dashboard');

export const getAllUsers = () => api.get('/admin/users');
export const getUser = (userId) => api.get(`/admin/users/${userId}`);

export const getAllMessages = () => api.get('/admin/messages');
export const getMessage = (messageId) => api.get(`/admin/messages/${messageId}`);

export const getPayments = (page = 0, size = 20) =>
  api.get('/admin/payments', { params: { page, size, sort: 'createdAt,desc' } });

export const getAllPlans = () => api.get('/admin/plans');

export const getContactMessages = () => api.get('/admin/contact-messages');
export const getContactMessage = (id) => api.get(`/admin/contact-messages/${id}`);

export const getAllContracts = () => api.get('/admin/contracts');
export const getContract = (id) => api.get(`/admin/contracts/${id}`);
