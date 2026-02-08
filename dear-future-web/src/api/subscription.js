import api from './axiosConfig';

/** Fiyatlandırma sayfası için plan listesi (Türkçe). Giriş gerekmez. */
export const getPlans = async () => {
    const response = await api.get('/subscription/plans');
    return response.data;
};

/**
 * Üyelik ödemesi başlat (iyzico checkout). Yanıtta paymentPageUrl döner; kullanıcı bu URL'e yönlendirilir.
 * @param {'PLUS'|'PREMIUM'} plan
 */
export const initializeCheckout = async (plan) => {
    const response = await api.post('/subscription/checkout/initialize', { plan });
    return response.data;
};

/** Aboneliği iptal et (plan FREE yapılır) */
export const cancelSubscription = async () => {
    return api.post('/subscription/cancel');
};
