import { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getMessageByViewToken } from '../api/message';
import './MessageViewPage.css';

const MessageViewPage = () => {
    const { viewToken } = useParams();
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!viewToken) {
            setError('Geçersiz bağlantı.');
            setLoading(false);
            return;
        }
        let cancelled = false;
        setLoading(true);
        setError(null);
        getMessageByViewToken(viewToken)
            .then((res) => {
                if (!cancelled) setData(res.data);
            })
            .catch((err) => {
                if (!cancelled) {
                    const status = err.response?.status;
                    const msg = err.response?.data?.message || err.response?.data;
                    if (status === 404) setError('Mesaj bulunamadı.');
                    else if (status === 403) setError(typeof msg === 'string' ? msg : 'Bu mesaj henüz açılmadı. Zamanı geldiğinde görüntüleyebilirsiniz.');
                    else setError('Mesaj yüklenemedi.');
                }
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });
        return () => { cancelled = true; };
    }, [viewToken]);

    if (loading) {
        return (
            <div className="message-view-container">
                <div className="message-view-loading">
                    <div className="message-view-spinner" />
                    <p>Mesaj yükleniyor...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="message-view-container">
                <div className="message-view-error">
                    <p>{error}</p>
                    <Link to="/settings" className="message-view-back">Ayarlara dön</Link>
                </div>
            </div>
        );
    }

    const formatDate = (instant) => {
        if (!instant) return '—';
        return new Date(instant).toLocaleDateString('tr-TR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    const contents = data?.contents ?? [];

    return (
        <div className="message-view-container">
            <div className="message-view-card">
                <div className="message-view-header">
                    {data?.senderName && (
                        <span className="message-view-sender">{data.senderName}</span>
                    )}
                    <span className="message-view-date">{formatDate(data?.scheduledAt)}</span>
                </div>

                <div className="message-view-body">
                    {contents.length === 0 ? (
                        <p className="message-view-empty">Bu mesajda görüntülenecek içerik yok.</p>
                    ) : (
                        contents.map((item, index) => {
                            if (item.type === 'TEXT' && item.textContent) {
                                return (
                                    <div key={index} className="message-view-block message-view-text">
                                        <div className="message-view-text-content">{item.textContent}</div>
                                    </div>
                                );
                            }
                            if (item.type === 'IMAGE' && item.fileUrl) {
                                return (
                                    <div key={index} className="message-view-block message-view-image">
                                        <img src={item.fileUrl} alt={item.fileName || 'Ek'} className="message-view-img" />
                                        {item.fileName && <p className="message-view-caption">{item.fileName}</p>}
                                    </div>
                                );
                            }
                            if ((item.type === 'FILE' || item.type === 'VIDEO' || item.type === 'AUDIO') && item.fileUrl) {
                                return (
                                    <div key={index} className="message-view-block message-view-file">
                                        <a href={item.fileUrl} target="_blank" rel="noopener noreferrer" className="message-view-file-link">
                                            {item.fileName || 'Dosyayı indir'}
                                        </a>
                                    </div>
                                );
                            }
                            return null;
                        })
                    )}
                </div>

                <div className="message-view-footer">
                    <Link to="/settings" className="message-view-back-link">← Ayarlara dön</Link>
                </div>
            </div>
        </div>
    );
};

export default MessageViewPage;
