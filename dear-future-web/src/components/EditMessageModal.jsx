import { useState, useEffect } from 'react';
import { FaTimes, FaSave, FaCalendar, FaUserFriends } from 'react-icons/fa';
import { updateMessage } from '../api/message';
import { toast } from 'react-toastify';
import './EditMessageModal.css';

const EditMessageModal = ({ message, onClose, onUpdate }) => {
    const [content, setContent] = useState('');
    const [scheduledAt, setScheduledAt] = useState('');
    const [receivers, setReceivers] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        if (message) {
            setContent(message.content || '');

            if (message.scheduledAt) {
                const date = new Date(message.scheduledAt);
                date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
                setScheduledAt(date.toISOString().slice(0, 16));
            }

            if (message.recipientEmails && message.recipientEmails.length) {
                setReceivers(message.recipientEmails.join(', '));
            } else {
                setReceivers('');
            }
        }
    }, [message]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const updateData = {
                content,
                scheduledAt: new Date(scheduledAt).toISOString(),
            };

            await updateMessage(message.id, updateData);
            toast.success('Mesaj güncellendi!');
            onUpdate(); // Refresh parent
            onClose();
        } catch (error) {
            console.error('Update failed:', error);
            toast.error('Güncelleme başarısız oldu.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <div className="modal-header">
                    <h3>Mesajı Düzenle</h3>
                    <button className="close-btn" onClick={onClose}>
                        <FaTimes />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="edit-form">
                    <div className="form-group">
                        <label>Mesaj İçeriği</label>
                        <textarea
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                            required
                            rows="5"
                            className="modern-textarea"
                        />
                    </div>

                    <div className="form-group">
                        <label>
                            <FaCalendar className="label-icon" />
                            Teslim Tarihi
                        </label>
                        <input
                            type="datetime-local"
                            value={scheduledAt}
                            onChange={(e) => setScheduledAt(e.target.value)}
                            required
                            className="modern-input"
                        />
                    </div>

                    <div className="form-group">
                        <label>
                            <FaUserFriends className="label-icon" />
                            Alıcılar (Virgülle ayırın)
                        </label>
                        <input
                            type="text"
                            value={receivers}
                            onChange={(e) => setReceivers(e.target.value)}
                            placeholder="ornek@email.com, diger@email.com"
                            className="modern-input"
                        />
                    </div>

                    <div className="modal-actions">
                        <button type="button" className="cancel-btn" onClick={onClose}>
                            İptal
                        </button>
                        <button type="submit" className="save-btn" disabled={isLoading}>
                            {isLoading ? 'Kaydediliyor...' : (
                                <>
                                    <FaSave /> Kaydet
                                </>
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EditMessageModal;
