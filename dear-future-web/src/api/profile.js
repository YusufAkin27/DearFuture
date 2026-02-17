import api from './axiosConfig';

export const getProfile = async () => {
    return api.get('/user/profile');
};

/** Plan bazlı mesaj kotası: limit, kullanılan, kalan (backend’den güncel). */
export const getMessageQuota = async () => {
    const res = await api.get('/user/message-quota');
    return res.data;
};

export const updateProfile = async (data) => {
    return api.put('/user/profile', data);
};

/** Ayarlar: dil, bildirimler */
export const updateSettings = async (data) => {
    return api.put('/user/settings', data);
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
    return api.post('/user/profile/photo', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });
};

export const deleteProfilePhoto = async () => {
    return api.delete('/user/profile/photo');
};
