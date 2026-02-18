import api from './axiosConfig';
import { invalidatePrefix } from './cache';

const USER_CACHE_PREFIX = 'get:/user';

export const getProfile = async () => {
    return api.get('/user/profile');
};

/** Plan bazlı mesaj kotası: limit, kullanılan, kalan (backend’den güncel). */
export const getMessageQuota = async () => {
    const res = await api.get('/user/message-quota');
    return res.data;
};

export const updateProfile = async (data) => {
    const res = await api.put('/user/profile', data);
    invalidatePrefix(USER_CACHE_PREFIX);
    return res;
};

/** Ayarlar: dil, bildirimler */
export const updateSettings = async (data) => {
    const res = await api.put('/user/settings', data);
    invalidatePrefix(USER_CACHE_PREFIX);
    return res;
};

/** Hesabı kalıcı sil (tüm veriler silinir) */
export const deleteAccount = async () => {
    return api.delete('/user/account');
};

/** Hesabı devre dışı bırak (giriş engellenir, veriler kalır) */
export const deactivateAccount = async () => {
    return api.patch('/user/account/deactivate');
};

/** Profil fotoğrafı yükle (FormData: photo = file) */
export const uploadProfilePhoto = async (file) => {
    const formData = new FormData();
    formData.append('photo', file);
    const res = await api.post('/user/profile/photo', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });
    invalidatePrefix(USER_CACHE_PREFIX);
    return res;
};

export const deleteProfilePhoto = async () => {
    const res = await api.delete('/user/profile/photo');
    invalidatePrefix(USER_CACHE_PREFIX);
    return res;
};
