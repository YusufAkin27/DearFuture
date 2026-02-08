import api from './axiosConfig';

/** Basit mesaj: metin + tarih, tek alıcı (kendin). FREE plan için. */
export const createMessage = async (data) => {
    return api.post('/messages', {
        content: data.content,
        scheduledAt: data.scheduledAt,
    });
};

/** Çoklu alıcı + içerik (metin, fotoğraf, dosya). PLUS/PREMIUM için. */
export const scheduleMessage = async (data) => {
    return api.post('/messages/schedule', {
        recipientEmails: data.recipientEmails,
        scheduledAt: data.scheduledAt,
        contents: data.contents,
    });
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

export const updateMessage = async (id, data) => {
    return api.put(`/messages/${id}`, data);
};

export const deleteMessage = async (id) => {
    return api.delete(`/messages/${id}`);
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
