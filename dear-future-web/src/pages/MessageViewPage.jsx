import { useState, useEffect, useCallback } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getMessageByViewToken } from '../api/message';
import Stack from '../components/Stack';
import Folder from '../components/Folder';
import './MessageViewPage.css';

/** Dosya/fotoğrafı blob olarak indirir; sayfaya yönlendirmez. */
const downloadBlob = async (url, fileName) => {
    const res = await fetch(url, { credentials: 'include', mode: 'cors' });
    if (!res.ok) throw new Error('İndirilemedi');
    const blob = await res.blob();
    const name = fileName || res.headers.get('content-disposition')?.match(/filename="?([^";]+)"?/)?.[1] || url.split('/').pop() || 'download';
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = name;
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(a.href);
};

const MessageViewPage = () => {
    const { viewToken } = useParams();
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [downloading, setDownloading] = useState(null);

    const handleDownload = useCallback(async (e, fileUrl, fileName) => {
        e.preventDefault();
        e.stopPropagation();
        if (!fileUrl || downloading) return;
        const key = `${fileUrl}-${fileName || ''}`;
        setDownloading(key);
        try {
            await downloadBlob(fileUrl, fileName || 'dosya');
        } catch (err) {
            console.error(err);
            window.open(fileUrl, '_blank');
        } finally {
            setDownloading(null);
        }
    }, [downloading]);

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
    const textContents = contents.filter((c) => c.type === 'TEXT' && c.textContent);
    const imageContents = contents.filter((c) => c.type === 'IMAGE' && c.fileUrl);
    const fileContents = contents.filter((c) => (c.type === 'FILE' || c.type === 'VIDEO' || c.type === 'AUDIO') && c.fileUrl);

    const stackCards = imageContents.map((item, i) => (
        <div key={i} className="message-view-stack-card-inner">
            <img
                src={item.fileUrl}
                alt={item.fileName || 'Fotoğraf'}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
            <button
                type="button"
                className="stack-card-download"
                onClick={(e) => handleDownload(e, item.fileUrl, item.fileName)}
                disabled={!!downloading}
            >
                {downloading === `${item.fileUrl}-${item.fileName || ''}` ? 'İndiriliyor…' : 'İndir'}
            </button>
        </div>
    ));

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
                        <>
                            {textContents.map((item, index) => (
                                <div key={`t-${index}`} className="message-view-block message-view-text">
                                    <div className="message-view-text-content">{item.textContent}</div>
                                </div>
                            ))}

                            {imageContents.length > 0 && (
                                <div className="message-view-block message-view-section message-view-photos-section">
                                    <p className="message-view-section-label">Fotoğraflar</p>
                                    <div className="message-view-stack-wrap">
                                        <div className="message-view-stack-size" style={{ width: 280, height: 280 }}>
                                            <Stack
                                                key={viewToken}
                                                randomRotation={false}
                                                sensitivity={200}
                                                sendToBackOnClick
                                                cards={stackCards}
                                                autoplay={false}
                                            />
                                        </div>
                                    </div>
                                </div>
                            )}

                            {fileContents.length > 0 && (
                                <div className="message-view-block message-view-section message-view-files-section">
                                    <p className="message-view-section-label">Dosyalar</p>
                                    <div className="message-view-folder-wrap">
                                        <Folder
                                            size={2}
                                            color="#5227FF"
                                            className="message-view-folder"
                                            items={fileContents.slice(0, 3).map((item, index) => (
                                                <button
                                                    key={index}
                                                    type="button"
                                                    className="message-view-folder-paper-link"
                                                    onClick={(e) => handleDownload(e, item.fileUrl, item.fileName)}
                                                    disabled={!!downloading}
                                                >
                                                    {item.fileName || 'Dosya'}
                                                </button>
                                            ))}
                                        />
                                        {fileContents.length > 3 && (
                                            <div className="message-view-files-extra">
                                                <p className="message-view-files-label">Diğer dosyalar</p>
                                                {fileContents.slice(3).map((item, index) => (
                                                    <button
                                                        key={index}
                                                        type="button"
                                                        className="message-view-file-link"
                                                        onClick={(e) => handleDownload(e, item.fileUrl, item.fileName)}
                                                        disabled={!!downloading}
                                                    >
                                                        {item.fileName || 'Dosyayı indir'}
                                                        <span className="message-view-file-download-badge">İndir</span>
                                                    </button>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            )}
                        </>
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
