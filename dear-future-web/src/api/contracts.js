import api from './axiosConfig';

/**
 * Tüm aktif sözleşmeleri getirir (liste).
 */
export const getContracts = () => api.get('/contracts');

/**
 * Türüne göre en güncel aktif sözleşmeyi getirir.
 * @param {string} type - ContractType: GIZLILIK, KULLANIM, CEREZ, KVKK, SATIS, IADE, KARGO
 */
export const getContractByType = (type) => api.get(`/contracts/type/${type}`);

/**
 * ID ile sözleşme getirir.
 */
export const getContractById = (id) => api.get(`/contracts/${id}`);
