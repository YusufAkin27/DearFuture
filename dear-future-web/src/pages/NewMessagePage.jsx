import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaPaperPlane, FaUser, FaImage, FaFile, FaMicrophone } from 'react-icons/fa';
import { getProfile } from '../api/profile';
import { getPendingMessages, getDeliveredMessages, createMessage, scheduleMessage, uploadMessageAttachment } from '../api/message';
import './NewMessagePage.css';

const PLAN_LIMITS = {
    FREE: { maxMessages: 3, maxRecipients: 1, photo: false, file: false, voice: false },
    PLUS: { maxMessages: 20, maxRecipients: 5, photo: true, file: true, voice: false },
    PREMIUM: { maxMessages: 100, maxRecipients: 20, photo: true, file: true, voice: true },
};

const NewMessagePage = () => {
    const navigate = useNavigate();
    const [profile, setProfile] = useState(null);
    const [pendingCount, setPendingCount] = useState(0);
    const [totalCount, setTotalCount] = useState(0);
    const [loading, setLoading] = useState(true);
    const [content, setContent] = useState('');
    const [scheduledAt, setScheduledAt] = useState('');
    const [recipients, setRecipients] = useState(['']);
    const [submitting, setSubmitting] = useState(false);
    const [photos, setPhotos] = useState([]);
    const [files, setFiles] = useState([]);
    const [uploadingPhoto, setUploadingPhoto] = useState(false);
    const [uploadingFile, setUploadingFile] = useState(false);

    useEffect(() => {
        let cancelled = false;
        const load = async () => {
            try {
                const [profileRes, pendingRes, deliveredRes] = await Promise.all([
                    getProfile(),
                    getPendingMessages(),
                    getDeliveredMessages(),
                ]);
                if (cancelled) return;
                const user = profileRes.data;
                const isExpired = user.subscriptionEndsAt && new Date(user.subscriptionEndsAt) < new Date();
                const plan = isExpired ? 'FREE' : (user.subscriptionPlan || 'FREE');
                setProfile({ ...user, effectivePlan: plan });
                const pendingList = Array.isArray(pendingRes.data) ? pendingRes.data : [];
                const deliveredList = Array.isArray(deliveredRes.data) ? deliveredRes.data : [];
                setPendingCount(pendingList.length);
                setTotalCount(pendingList.length + deliveredList.length);
                if (plan !== 'FREE') {
                    const maxR = PLAN_LIMITS[plan]?.maxRecipients ?? 1;
                    setRecipients([user.email || '', ...Array(Math.max(0, maxR - 1)).fill('')]);
                }
            } catch (err) {
                if (!cancelled) {
                    toast.error('Bilgiler yüklenemedi.');
                    navigate('/login', { replace: true });
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };
        load();
        return () => { cancelled = true; };
    }, [navigate]);

    const effectivePlan = profile?.effectivePlan || 'FREE';
    const limits = PLAN_LIMITS[effectivePlan] || PLAN_LIMITS.FREE;
    const canAddMore = effectivePlan === 'FREE'
        ? totalCount < limits.maxMessages
        : pendingCount < limits.maxMessages;

    const addRecipient = () => {
        if (recipients.length >= limits.maxRecipients) return;
        setRecipients([...recipients, '']);
    };

    const removeRecipient = (index) => {
        if (recipients.length <= 1) return;
        setRecipients(recipients.filter((_, i) => i !== index));
    };

    const setRecipient = (index, value) => {
        const next = [...recipients];
        next[index] = value;
        setRecipients(next);
    };

    const maxPhotos = profile?.maxPhotosPerMessage ?? 0;
    const maxPhotoSizeBytes = profile?.maxPhotoSizeBytes ?? 0;
    const maxFiles = profile?.maxFilesPerMessage ?? 0;
    const maxFileSizeBytes = profile?.maxFileSizeBytes ?? 0;

    const handlePhotoSelect = async (e) => {
        const selected = Array.from(e.target.files || []);
        if (selected.length === 0) return;
        if (photos.length + selected.length > maxPhotos) {
            toast.error(`En fazla ${maxPhotos} fotoğraf ekleyebilirsiniz.`);
            return;
        }
        for (const file of selected) {
            if (maxPhotoSizeBytes && file.size > maxPhotoSizeBytes) {
                toast.error(`Her fotoğraf en fazla ${Math.round(maxPhotoSizeBytes / (1024 * 1024))} MB olabilir.`);
                continue;
            }
            setUploadingPhoto(true);
            try {
                const res = await uploadMessageAttachment(file, 'IMAGE');
                setPhotos((prev) => [...prev, { url: res.data.url, fileName: res.data.fileName, fileSize: res.data.fileSize }]);
            } catch (err) {
                toast.error(err.response?.data?.message || 'Fotoğraf yüklenemedi.');
            } finally {
                setUploadingPhoto(false);
            }
        }
        e.target.value = '';
    };

    const handleFileSelect = async (e) => {
        const selected = Array.from(e.target.files || []);
        if (selected.length === 0) return;
        if (files.length + selected.length > maxFiles) {
            toast.error(`En fazla ${maxFiles} dosya ekleyebilirsiniz.`);
            return;
        }
        for (const file of selected) {
            if (maxFileSizeBytes && file.size > maxFileSizeBytes) {
                toast.error(`Her dosya en fazla ${Math.round(maxFileSizeBytes / (1024 * 1024))} MB olabilir.`);
                continue;
            }
            setUploadingFile(true);
            try {
                const res = await uploadMessageAttachment(file, 'FILE');
                setFiles((prev) => [...prev, { url: res.data.url, fileName: res.data.fileName, fileSize: res.data.fileSize }]);
            } catch (err) {
                toast.error(err.response?.data?.message || 'Dosya yüklenemedi.');
            } finally {
                setUploadingFile(false);
            }
        }
        e.target.value = '';
    };

    const removePhoto = (index) => setPhotos((prev) => prev.filter((_, i) => i !== index));
    const removeFile = (index) => setFiles((prev) => prev.filter((_, i) => i !== index));

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!content.trim() || !scheduledAt) {
            toast.error('Mesaj metni ve iletilme tarihi zorunludur.');
            return;
        }
        const at = new Date(scheduledAt);
        if (at <= new Date()) {
            toast.error('İletim tarihi gelecekte olmalıdır.');
            return;
        }
        if (!canAddMore) {
            toast.error(effectivePlan === 'FREE'
                ? 'Ücretsiz hesapta bekleyen ve iletilen mesajların toplamı en fazla 3 olabilir. Yeni mesaj kaydedemezsiniz.'
                : `Planınızda en fazla ${limits.maxMessages} zamanlanmış mesaj olabilir.`);
            return;
        }

        setSubmitting(true);
        try {
            const scheduledAtISO = at.toISOString();
            if (effectivePlan === 'FREE') {
                await createMessage({ content: content.trim(), scheduledAt: scheduledAtISO });
                toast.success('Mesajınız zamanlandı.');
            } else {
                const emails = recipients.map((e) => e.trim()).filter(Boolean);
                if (emails.length === 0) {
                    toast.error('En az bir alıcı e-posta adresi girin.');
                    setSubmitting(false);
                    return;
                }
                if (emails.length > limits.maxRecipients) {
                    toast.error(`Planınızda mesaj başına en fazla ${limits.maxRecipients} alıcı seçebilirsiniz.`);
                    setSubmitting(false);
                    return;
                }
                const contents = [
                    { type: 'TEXT', text: content.trim() },
                    ...photos.map((p) => ({ type: 'IMAGE', fileUrl: p.url, fileName: p.fileName, fileSize: p.fileSize })),
                    ...files.map((f) => ({ type: 'FILE', fileUrl: f.url, fileName: f.fileName, fileSize: f.fileSize })),
                ];
                await scheduleMessage({
                    recipientEmails: emails,
                    scheduledAt: scheduledAtISO,
                    contents,
                });
                toast.success('Mesajınız zamanlandı.');
            }
            navigate('/');
        } catch (error) {
            const msg = error.response?.data?.message || 'Mesaj kaydedilemedi.';
            toast.error(msg);
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="new-message-container">
                <div className="new-message-loading">
                    <div className="new-message-spinner" />
                    <p>Yükleniyor...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="new-message-container">
            <header className="new-message-header">
                <h2>Geleceğe Mesaj Yaz</h2>
                <p className="new-message-subtitle">Kendinize veya sevdiklerinize zamanlanmış mesaj bırakın.</p>
                <div className="new-message-plan-info">
                    <span className="plan-badge">{effectivePlan}</span>
                    <span className="plan-usage">
                        {effectivePlan === 'FREE'
                            ? `${totalCount} / ${limits.maxMessages} mesaj`
                            : `${pendingCount} / ${limits.maxMessages} zamanlanmış mesaj`}
                    </span>
                    {!canAddMore && (
                        <span className="plan-limit-warn">Limit doldu. Yeni mesaj için planınızı yükseltin veya mevcut bir mesajı silin.</span>
                    )}
                </div>
            </header>

            <form onSubmit={handleSubmit} className="new-message-form">
                <div className="form-group">
                    <label>Mesaj metni</label>
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        rows={8}
                        placeholder="Merhaba gelecekteki ben..."
                        className="form-textarea"
                        required
                    />
                </div>

                <div className="form-group">
                    <label>
                        <FaUser /> Mesajı ileteceğim e-posta adresini girin
                        {effectivePlan !== 'FREE' && (
                            <span className="label-hint"> (en fazla {limits.maxRecipients} adres)</span>
                        )}
                    </label>
                    {effectivePlan === 'FREE' ? (
                        <input
                            type="email"
                            value=""
                            readOnly
                            className="form-input recipient-input recipient-readonly"
                            placeholder="E-posta adresi"
                        />
                    ) : (
                        <>
                            {recipients.map((email, index) => (
                                <div key={index} className="recipient-row">
                                    <input
                                        type="email"
                                        value={email}
                                        onChange={(e) => setRecipient(index, e.target.value)}
                                        placeholder="ornek@email.com"
                                        className="form-input recipient-input"
                                    />
                                    {recipients.length > 1 && (
                                        <button type="button" className="recipient-remove" onClick={() => removeRecipient(index)} aria-label="Alıcıyı kaldır">
                                            ×
                                        </button>
                                    )}
                                </div>
                            ))}
                            {recipients.length < limits.maxRecipients && (
                                <button type="button" className="recipient-add" onClick={addRecipient}>
                                    + Alıcı ekle
                                </button>
                            )}
                        </>
                    )}
                </div>

                <div className="form-group">
                    <label>İletim tarihi ve saati</label>
                    <input
                        type="datetime-local"
                        value={scheduledAt}
                        onChange={(e) => setScheduledAt(e.target.value)}
                        className="form-input"
                        required
                    />
                </div>

                {limits.photo && maxPhotos > 0 && (
                    <div className="form-group">
                        <label><FaImage /> Fotoğraf ekle (en fazla {maxPhotos} adet, her biri max {Math.round(maxPhotoSizeBytes / (1024 * 1024))} MB)</label>
                        <input
                            type="file"
                            accept="image/*"
                            multiple
                            onChange={handlePhotoSelect}
                            disabled={uploadingPhoto || photos.length >= maxPhotos}
                            className="form-input file-input"
                        />
                        {photos.length > 0 && (
                            <ul className="attachment-list">
                                {photos.map((p, i) => (
                                    <li key={i} className="attachment-item">
                                        <img src={p.url} alt="" className="attachment-thumb" />
                                        <span className="attachment-name">{p.fileName}</span>
                                        <button type="button" className="attachment-remove" onClick={() => removePhoto(i)} aria-label="Kaldır">×</button>
                                    </li>
                                ))}
                            </ul>
                        )}
                        {uploadingPhoto && <p className="uploading-hint">Yükleniyor...</p>}
                    </div>
                )}

                {limits.file && maxFiles > 0 && (
                    <div className="form-group">
                        <label><FaFile /> Dosya ekle (en fazla {maxFiles} adet, her biri max {Math.round(maxFileSizeBytes / (1024 * 1024))} MB)</label>
                        <input
                            type="file"
                            multiple
                            onChange={handleFileSelect}
                            disabled={uploadingFile || files.length >= maxFiles}
                            className="form-input file-input"
                        />
                        {files.length > 0 && (
                            <ul className="attachment-list">
                                {files.map((f, i) => (
                                    <li key={i} className="attachment-item">
                                        <FaFile className="attachment-icon" />
                                        <span className="attachment-name">{f.fileName}</span>
                                        <button type="button" className="attachment-remove" onClick={() => removeFile(i)} aria-label="Kaldır">×</button>
                                    </li>
                                ))}
                            </ul>
                        )}
                        {uploadingFile && <p className="uploading-hint">Yükleniyor...</p>}
                    </div>
                )}

                {limits.voice && (
                    <div className="form-group plan-features-note">
                        <p className="plan-features-title"><FaMicrophone /> Ses kaydı (yakında)</p>
                    </div>
                )}

                <button type="submit" className="submit-btn" disabled={!canAddMore || submitting}>
                    <FaPaperPlane /> {submitting ? 'Kaydediliyor...' : 'Mesajı Zamanla'}
                </button>
            </form>
        </div>
    );
};

export default NewMessagePage;
