import api from './axiosConfig';

/** Fiyatlandırma sayfası için plan listesi (Türkçe). Giriş gerekmez. */
export const getPlans = async () => {
    const response = await api.get('/subscription/plans');
    return response.data;
};

/** Plan detayı (kod ile): fiyat, açıklama, mesaj/dosya/fotoğraf/ses limitleri. */
export const getPlanByCode = async (code) => {
    const response = await api.get(`/subscription/plans/${encodeURIComponent(code)}`);
    return response.data;
};

/**
 * Üyelik ödemesi başlat (iyzico checkout). Yanıtta paymentPageUrl döner; kullanıcı bu URL'e yönlendirilir.
 * @param {'PLUS'|'PREMIUM'} planCode
 */
export const initializeCheckout = async (planCode) => {
    const response = await api.post('/subscription/checkout/initialize', { planCode });
    return response.data;
};

/** Aboneliği iptal et (plan FREE yapılır) */
export const cancelSubscription = async () => {
    return api.post('/subscription/cancel');
};
