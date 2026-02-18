import { useState, useEffect, useCallback } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getMessageByViewToken } from '../api/message';
import Stack from '../components/Stack';
import Folder from '../components/Folder';
import { useDebouncedCallback } from '../hooks/useDebouncedCallback';
import './MessageViewPage.css';

/** Uzantıya göre MIME tipi döndürür (yüklenen tipte indirme için). */
const getMimeFromExtension = (fileName) => {
    if (!fileName || typeof fileName !== 'string') return null;
    const ext = fileName.split('.').pop()?.toLowerCase();
    const map = {
        // Görsel
        jpg: 'image/jpeg',
        jpeg: 'image/jpeg',
        png: 'image/png',
        gif: 'image/gif',
        webp: 'image/webp',
        avif: 'image/avif',
        svg: 'image/svg+xml',
        ico: 'image/x-icon',
        bmp: 'image/bmp',
        // Video
        mp4: 'video/mp4',
        webm: 'video/webm',
        mov: 'video/quicktime',
        avi: 'video/x-msvideo',
        mkv: 'video/x-matroska',
        m4v: 'video/x-m4v',
        // Ses
        mp3: 'audio/mpeg',
        wav: 'audio/wav',
        ogg: 'audio/ogg',
        m4a: 'audio/mp4',
        aac: 'audio/aac',
        webm: 'audio/webm',
        opus: 'audio/opus',
        // Dosya
        pdf: 'application/pdf',
        doc: 'application/msword',
        docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        xls: 'application/vnd.ms-excel',
        xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        ppt: 'application/vnd.ms-powerpoint',
        pptx: 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
        zip: 'application/zip',
        rar: 'application/vnd.rar',
        '7z': 'application/x-7z-compressed',
        txt: 'text/plain',
        rtf: 'application/rtf',
        csv: 'text/csv',
        md: 'text/markdown',
        json: 'application/json',
        xml: 'application/xml',
        epub: 'application/epub+zip',
    };
    return map[ext] || null;
};

/** İndirilen dosyayı orijinal tipinde kaydeder (fotoğraf, video, ses, dosya). */
const downloadBlob = async (url, fileName) => {
    const res = await fetch(url, { credentials: 'include', mode: 'cors' });
    if (!res.ok) throw new Error('İndirilemedi');
    const data = await res.arrayBuffer();
    const responseType = res.headers.get('content-type')?.split(';')[0]?.trim();
    const inferredType = getMimeFromExtension(fileName);
    const mimeType = (responseType && responseType !== 'application/octet-stream') ? responseType : (inferredType || 'application/octet-stream');
    const blob = new Blob([data], { type: mimeType });
    const name = fileName || res.headers.get('content-disposition')?.match(/filename="?([^";]+)"?/)?.[1] || url.split('/').pop()?.split('?')[0] || 'download';
    const safeName = name.includes('.') ? name : `${name}.${mimeType.split('/')[1] || 'bin'}`;
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = safeName;
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

    const debouncedDownload = useDebouncedCallback(handleDownload, 500);

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
    const audioContents = contents.filter((c) => c.type === 'AUDIO' && c.fileUrl);
    const fileContents = contents.filter((c) => (c.type === 'FILE' || c.type === 'VIDEO') && c.fileUrl);

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
                onClick={(e) => debouncedDownload(e, item.fileUrl, item.fileName)}
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
                                        <div className="message-view-stack-size">
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

                            {audioContents.length > 0 && (
                                <div className="message-view-block message-view-section message-view-audio-section">
                                    <p className="message-view-section-label">Ses kaydı</p>
                                    <div className="message-view-audio-list">
                                        {audioContents.map((item, index) => (
                                            <div key={index} className="message-view-audio-item">
                                                <audio
                                                    src={item.fileUrl}
                                                    controls
                                                    className="message-view-audio-player"
                                                    preload="metadata"
                                                />
                                                {item.fileName && (
                                                    <span className="message-view-audio-filename">{item.fileName}</span>
                                                )}
                                                <button
                                                    type="button"
                                                    className="message-view-audio-download"
                                                    onClick={(e) => debouncedDownload(e, item.fileUrl, item.fileName)}
                                                    disabled={!!downloading}
                                                >
                                                    {downloading === `${item.fileUrl}-${item.fileName || ''}` ? 'İndiriliyor…' : 'İndir'}
                                                </button>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {fileContents.length > 0 && (
                                <div className="message-view-block message-view-section message-view-files-section">
                                    <p className="message-view-section-label">Dosyalar</p>
                                    <div className="message-view-folder-wrap">
                                        <div className="message-view-folder-scaler">
                                            <Folder
                                                size={2}
                                                color="#5227FF"
                                                className="message-view-folder"
                                                items={fileContents.slice(0, 3).map((item, index) => (
                                                    <button
                                                        key={index}
                                                        type="button"
                                                        className="message-view-folder-paper-link"
                                                        onClick={(e) => debouncedDownload(e, item.fileUrl, item.fileName)}
                                                        disabled={!!downloading}
                                                    >
                                                        {item.fileName || 'Dosya'}
                                                    </button>
                                                ))}
                                            </Folder>
                                        </div>
                                        {fileContents.length > 3 && (
                                            <div className="message-view-files-extra">
                                                <p className="message-view-files-label">Diğer dosyalar</p>
                                                {fileContents.slice(3).map((item, index) => (
                                                    <button
                                                        key={index}
                                                        type="button"
                                                        className="message-view-file-link"
                                                        onClick={(e) => debouncedDownload(e, item.fileUrl, item.fileName)}
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
