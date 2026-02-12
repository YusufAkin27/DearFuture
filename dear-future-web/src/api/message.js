import api from './axiosConfig';

/** Basit mesaj: metin + tarih, tek alıcı (kendin). FREE plan için. */
export const createMessage = async (data) => {
    const body = { content: data.content, scheduledAt: data.scheduledAt };
    if (data.isPublic != null) body.isPublic = data.isPublic;
    return api.post('/messages', body);
};

/** Çoklu alıcı + içerik (metin, fotoğraf, dosya). PLUS/PREMIUM için. */
export const scheduleMessage = async (data) => {
    const body = {
        recipientEmails: data.recipientEmails,
        scheduledAt: data.scheduledAt,
        contents: data.contents,
    };
    if (data.isPublic != null) body.isPublic = data.isPublic;
    return api.post('/messages/schedule', body);
};

export const getPendingMessages = async () => {
    return api.get('/messages/pending');
};

export const getDeliveredMessages = async () => {
    return api.get('/messages');
};

export const getMessage = async (id) => {
    return api.get(`/messages/${id}`);
};

/** Token ile mesajı görüntüle (iletilen mesajın özel sayfası; auth gerektirmez). */
export const getMessageByViewToken = async (viewToken) => {
    return api.get(`/messages/view/${viewToken}`);
};

export const updateMessage = async (id, data) => {
    return api.put(`/messages/${id}`, data);
};

export const deleteMessage = async (id) => {
    return api.delete(`/messages/${id}`);
};

/** Herkese açık mesajlar (sayfalı). Giriş yoksa starredByMe null. */
export const getPublicMessages = async (page = 0, size = 12) => {
    return api.get('/messages/public', { params: { page, size } });
};

/** Yıldızladığım mesajlar (giriş gerekir). */
export const getMyStarredMessages = async () => {
    return api.get('/messages/public/starred');
};

/** Mesajı yıldızla (giriş gerekir). */
export const starPublicMessage = async (messageId) => {
    return api.post(`/messages/public/${messageId}/star`);
};

/** Yıldızı kaldır (giriş gerekir). */
export const unstarPublicMessage = async (messageId) => {
    return api.delete(`/messages/public/${messageId}/star`);
};

/** Mesaj eki (fotoğraf veya dosya) yükler. PLUS/PREMIUM. */
export const uploadMessageAttachment = async (file, type) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    return api.post('/messages/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });
};
