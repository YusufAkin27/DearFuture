import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaPaperPlane, FaUser, FaImage, FaFile, FaMicrophone, FaTrash } from 'react-icons/fa';
import { PiFilesThin } from 'react-icons/pi';
import { getProfile, getMessageQuota } from '../api/profile';
import { createMessage, scheduleMessage, uploadMessageAttachment } from '../api/message';
import './NewMessagePage.css';

/** Profil/kota gelmezse kullanılacak varsayılanlar (sadece yedek) */
const FALLBACK_LIMITS = { maxMessages: 3, maxRecipients: 1, photo: false, file: false, voice: false };

const NewMessagePage = () => {
    const navigate = useNavigate();
    const photoInputRef = useRef(null);
    const fileInputRef = useRef(null);
    const [profile, setProfile] = useState(null);
    const [quota, setQuota] = useState(null);
    const [loading, setLoading] = useState(true);
    const [content, setContent] = useState('');
    const [scheduledAt, setScheduledAt] = useState('');
    const [recipients, setRecipients] = useState(['']);
    const [submitting, setSubmitting] = useState(false);
    const [photos, setPhotos] = useState([]);
    const [files, setFiles] = useState([]);
    const [uploadingPhoto, setUploadingPhoto] = useState(false);
    const [uploadingFile, setUploadingFile] = useState(false);
    const [isPublic, setIsPublic] = useState(false);
    const [voiceRecording, setVoiceRecording] = useState(null);
    const [isRecording, setIsRecording] = useState(false);
    const [isUploadingVoice, setIsUploadingVoice] = useState(false);
    const mediaRecorderRef = useRef(null);
    const audioChunksRef = useRef([]);
    const mediaStreamRef = useRef(null);

    useEffect(() => {
        let cancelled = false;
        const load = async () => {
            try {
                const [profileRes, quotaRes] = await Promise.all([
                    getProfile(),
                    getMessageQuota(),
                ]);
                if (cancelled) return;
                const user = profileRes.data;
                const isExpired = user.subscriptionEndsAt && new Date(user.subscriptionEndsAt) < new Date();
                const plan = isExpired ? 'FREE' : (user.subscriptionPlanCode ?? user.subscriptionPlan ?? 'FREE');
                setProfile({ ...user, effectivePlan: plan });
                setQuota(quotaRes && typeof quotaRes === 'object' ? quotaRes : null);
                if (plan !== 'FREE') {
                    setRecipients([user.email || '']);
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
    const limit = quota?.limit ?? profile?.maxMessagesPerPlan ?? FALLBACK_LIMITS.maxMessages;
    const used = quota?.used ?? 0;
    const remaining = quota?.remaining ?? 0;
    const maxRecipients = profile?.maxRecipientsPerMessage ?? FALLBACK_LIMITS.maxRecipients;
    const limits = {
        maxMessages: limit,
        maxRecipients,
        photo: (profile?.maxPhotosPerMessage ?? 0) > 0,
        file: (profile?.maxFilesPerMessage ?? 0) > 0,
        voice: profile?.allowVoice === true,
    };
    const canAddMore = remaining > 0;

    const addRecipient = () => {
                if (recipients.length >= maxRecipients) return;
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
    const maxAudioPerMessage = profile?.maxAudioPerMessage ?? (limits.voice ? 1 : 0);
    const maxAudioSizeBytes = profile?.maxAudioSizeBytes ?? (limits.voice ? 10 * 1024 * 1024 : 0);

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

    const startVoiceRecording = async () => {
        if (!limits.voice || voiceRecording || isRecording) return;
        audioChunksRef.current = [];
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            mediaStreamRef.current = stream;
            const mimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus') ? 'audio/webm;codecs=opus' : 'audio/webm';
            const recorder = new MediaRecorder(stream);
            mediaRecorderRef.current = recorder;
            recorder.ondataavailable = (e) => { if (e.data.size > 0) audioChunksRef.current.push(e.data); };
            recorder.onstop = async () => {
                mediaStreamRef.current?.getTracks().forEach((t) => t.stop());
                mediaStreamRef.current = null;
                const blob = new Blob(audioChunksRef.current, { type: mimeType });
                const ext = mimeType.includes('opus') || mimeType.includes('webm') ? 'webm' : 'ogg';
                const file = new File([blob], `recording.${ext}`, { type: blob.type });
                if (maxAudioSizeBytes && file.size > maxAudioSizeBytes) {
                    toast.error(`Ses kaydı en fazla ${Math.round(maxAudioSizeBytes / (1024 * 1024))} MB olabilir.`);
                    setIsRecording(false);
                    return;
                }
                setIsUploadingVoice(true);
                try {
                    const res = await uploadMessageAttachment(file, 'AUDIO');
                    setVoiceRecording({ url: res.data.url, fileName: res.data.fileName, fileSize: res.data.fileSize });
                } catch (err) {
                    toast.error(err.response?.data?.message || 'Ses yüklenemedi.');
                } finally {
                    setIsUploadingVoice(false);
                }
                setIsRecording(false);
            };
            recorder.start();
            setIsRecording(true);
        } catch (err) {
            toast.error('Mikrofon erişimi gerekli. Tarayıcı iznini verin.');
        }
    };

    const stopVoiceRecording = () => {
        if (mediaRecorderRef.current?.state === 'recording') {
            mediaRecorderRef.current.stop();
        }
    };

    const removeVoiceRecording = () => setVoiceRecording(null);

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
                    toast.error(remaining <= 0
                        ? 'Mesaj hakkınız doldu. Yeni mesaj için planınızı yükseltin.'
                        : 'Yeni mesaj ekleyemezsiniz.');
                    return;
                }

        setSubmitting(true);
        try {
            const scheduledAtISO = at.toISOString();
            if (effectivePlan === 'FREE') {
                await createMessage({ content: content.trim(), scheduledAt: scheduledAtISO, isPublic });
                toast.success('Mesajınız zamanlandı.');
            } else {
                const emails = recipients.map((e) => e.trim()).filter(Boolean);
                if (emails.length === 0) {
                    toast.error('En az bir alıcı e-posta adresi girin.');
                    setSubmitting(false);
                    return;
                }
                if (emails.length > maxRecipients) {
                    toast.error(`Planınızda mesaj başına en fazla ${maxRecipients} alıcı seçebilirsiniz.`);
                    setSubmitting(false);
                    return;
                }
                const contents = [
                    { type: 'TEXT', text: content.trim() },
                    ...photos.map((p) => ({ type: 'IMAGE', fileUrl: p.url, fileName: p.fileName, fileSize: p.fileSize })),
                    ...files.map((f) => ({ type: 'FILE', fileUrl: f.url, fileName: f.fileName, fileSize: f.fileSize })),
                    ...(voiceRecording ? [{ type: 'AUDIO', fileUrl: voiceRecording.url, fileName: voiceRecording.fileName, fileSize: voiceRecording.fileSize }] : []),
                ];
                await scheduleMessage({
                    recipientEmails: emails,
                    scheduledAt: scheduledAtISO,
                    contents,
                    isPublic,
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
                    <span className="plan-badge">{quota?.planName ?? effectivePlan}</span>
                    <span className="plan-usage">
                        {effectivePlan === 'FREE'
                            ? `${used} / ${limit} mesaj (toplam bekleyen + iletilen)`
                            : `${used} / ${limit} zamanlanmış mesaj · Kalan: ${remaining}`}
                    </span>
                    {canAddMore && (
                        <span className="new-message-allow-hint">
                            {effectivePlan === 'FREE'
                                ? `Ücretsiz hesapta toplam ${limit} mesaj hakkınız var. ${remaining} hakkınız kaldı.`
                                : `Bu dönemde ${limit} mesaj hakkınız var. ${remaining} hakkınız kaldı.`}
                        </span>
                    )}
                    {!canAddMore && (
                        <span className="plan-limit-warn">Mesaj hakkınız doldu. Yeni mesaj için planı yükseltin .</span>
                    )}
                </div>
                <div className="plan-features-summary">
                    <ul>
                        <li>Toplam {limit} mesaj hakkı · Kalan: {remaining}</li>
                        <li>
                            Metin
                            {maxPhotos > 0 && ` · Fotoğraf (en fazla ${maxPhotos} adet, ${Math.round(maxPhotoSizeBytes / (1024 * 1024))} MB)`}
                            {maxFiles > 0 && ` · Dosya (en fazla ${maxFiles} adet, ${Math.round(maxFileSizeBytes / (1024 * 1024))} MB)`}
                            {limits.voice && ` · Ses kaydı (en fazla ${maxAudioPerMessage} adet, ${Math.round(maxAudioSizeBytes / (1024 * 1024))} MB)`}
                        </li>
                        <li>Mesaj başına en fazla {maxRecipients} alıcı · Herkese açık seçeneği</li>
                    </ul>
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
                            <span className="label-hint"> (en fazla {maxRecipients} adres)</span>
                        )}
                    </label>
                    {effectivePlan === 'FREE' ? (
                        <>
                            <p className="form-hint recipient-free-hint">Bu mesaj size iletilecek (e-posta: {profile?.email || '—'})</p>
                            <input
                                type="email"
                                value={profile?.email || ''}
                                readOnly
                                className="form-input recipient-input recipient-readonly"
                                placeholder="E-posta adresi"
                            />
                        </>
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
                            {recipients.length < maxRecipients && (
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

                <div className="form-group checkbox-group">
                    <label className="toggle-switch">
                        <input
                            type="checkbox"
                            checked={isPublic}
                            onChange={(e) => setIsPublic(e.target.checked)}
                        />
                        <span className="slider round" />
                    </label>
                    <div className="toggle-label">
                        <span>Herkese açık</span>
                        <p>Mesaj iletildikten sonra herkese açık sayfada listelensin.</p>
                    </div>
                </div>

                {limits.photo && maxPhotos > 0 && (
                    <div className="form-group file-input-block">
                        <label className="file-input-label"><FaImage /> Fotoğraf ekle (en fazla {maxPhotos} adet, her biri max {Math.round(maxPhotoSizeBytes / (1024 * 1024))} MB)</label>
                        <input
                            ref={photoInputRef}
                            type="file"
                            accept="image/*"
                            multiple
                            onChange={handlePhotoSelect}
                            disabled={uploadingPhoto || photos.length >= maxPhotos}
                            className="file-input-hidden"
                            aria-hidden="true"
                        />
                        {photos.length === 0 ? (
                            <div
                                className={`file-upload-zone ${uploadingPhoto || photos.length >= maxPhotos ? 'file-upload-zone--disabled' : ''}`}
                                onClick={() => { if (!uploadingPhoto && photos.length < maxPhotos) photoInputRef.current?.click(); }}
                                role="button"
                                tabIndex={0}
                                onKeyDown={(e) => { if ((e.key === 'Enter' || e.key === ' ') && !uploadingPhoto && photos.length < maxPhotos) photoInputRef.current?.click(); }}
                            >
                                <h3 className="file-upload-title">Dosyalarınızı yükleyin</h3>
                                <p className="file-upload-subtitle">JPG, PNG, JPEG</p>
                                <div className="file-upload-zone-inner">
                                    <PiFilesThin className="file-upload-icon" aria-hidden />
                                </div>
                            </div>
                        ) : (
                            <div className="file-upload-previews-wrap">
                                <div className="file-upload-previews file-upload-previews--photos">
                                    {photos.map((p, i) => (
                                        <div key={i} className="file-upload-preview-item">
                                            <img src={p.url} alt="" className="file-upload-preview-img" />
                                            <button
                                                type="button"
                                                className="file-upload-delete"
                                                onClick={(e) => { e.stopPropagation(); removePhoto(i); }}
                                                aria-label="Kaldır"
                                            >
                                                <FaTrash />
                                            </button>
                                        </div>
                                    ))}
                                </div>
                                {photos.length < maxPhotos && (
                                    <>
                                        <div
                                            className={`file-upload-zone file-upload-zone--small ${uploadingPhoto ? 'file-upload-zone--disabled' : ''}`}
                                            onClick={() => { if (!uploadingPhoto) photoInputRef.current?.click(); }}
                                            role="button"
                                            tabIndex={0}
                                            onKeyDown={(e) => { if ((e.key === 'Enter' || e.key === ' ') && !uploadingPhoto) photoInputRef.current?.click(); }}
                                        >
                                            <PiFilesThin className="file-upload-icon file-upload-icon--small" aria-hidden />
                                        </div>
                                    </>
                                )}
                            </div>
                        )}
                        {uploadingPhoto && <p className="uploading-hint">Yükleniyor...</p>}
                    </div>
                )}

                {limits.file && maxFiles > 0 && (
                    <div className="form-group file-input-block">
                        <label className="file-input-label"><FaFile /> Dosya ekle (en fazla {maxFiles} adet, her biri max {Math.round(maxFileSizeBytes / (1024 * 1024))} MB)</label>
                        <input
                            ref={fileInputRef}
                            type="file"
                            multiple
                            onChange={handleFileSelect}
                            disabled={uploadingFile || files.length >= maxFiles}
                            className="file-input-hidden"
                            aria-hidden="true"
                        />
                        {files.length === 0 ? (
                            <div
                                className={`file-upload-zone ${uploadingFile || files.length >= maxFiles ? 'file-upload-zone--disabled' : ''}`}
                                onClick={() => { if (!uploadingFile && files.length < maxFiles) fileInputRef.current?.click(); }}
                                role="button"
                                tabIndex={0}
                                onKeyDown={(e) => { if ((e.key === 'Enter' || e.key === ' ') && !uploadingFile && files.length < maxFiles) fileInputRef.current?.click(); }}
                            >
                                <h3 className="file-upload-title">Dosyalarınızı yükleyin</h3>
                                <p className="file-upload-subtitle">PDF, DOC, ZIP vb.</p>
                                <div className="file-upload-zone-inner">
                                    <PiFilesThin className="file-upload-icon" aria-hidden />
                                </div>
                            </div>
                        ) : (
                            <div className="file-upload-previews-wrap">
                                <ul className="file-upload-list">
                                    {files.map((f, i) => (
                                        <li key={i} className="file-upload-list-item">
                                            <FaFile className="file-upload-list-icon" />
                                            <span className="file-upload-list-name">{f.fileName}</span>
                                            <button
                                                type="button"
                                                className="file-upload-delete file-upload-delete--list"
                                                onClick={() => removeFile(i)}
                                                aria-label="Kaldır"
                                            >
                                                <FaTrash />
                                            </button>
                                        </li>
                                    ))}
                                </ul>
                                {files.length < maxFiles && (
                                    <div
                                        className={`file-upload-zone file-upload-zone--small ${uploadingFile ? 'file-upload-zone--disabled' : ''}`}
                                        onClick={() => { if (!uploadingFile) fileInputRef.current?.click(); }}
                                        role="button"
                                        tabIndex={0}
                                        onKeyDown={(e) => { if ((e.key === 'Enter' || e.key === ' ') && !uploadingFile) fileInputRef.current?.click(); }}
                                    >
                                        <PiFilesThin className="file-upload-icon file-upload-icon--small" aria-hidden />
                                    </div>
                                )}
                            </div>
                        )}
                        {uploadingFile && <p className="uploading-hint">Yükleniyor...</p>}
                    </div>
                )}

                {limits.voice && maxAudioPerMessage > 0 && (
                    <div className="form-group file-input-block voice-recording-block">
                        <label className="file-input-label">
                            <FaMicrophone /> Ses kaydı (en fazla {maxAudioPerMessage} adet, en fazla {Math.round(maxAudioSizeBytes / (1024 * 1024))} MB)
                        </label>
                        {!voiceRecording && !isRecording && (
                            <button
                                type="button"
                                className="voice-record-btn"
                                onClick={startVoiceRecording}
                                disabled={isUploadingVoice}
                            >
                                <FaMicrophone /> Ses kaydet
                            </button>
                        )}
                        {isRecording && (
                            <div className="voice-recording-active">
                                <span className="voice-recording-dot" aria-hidden />
                                <span>Kaydediliyor...</span>
                                <button type="button" className="voice-stop-btn" onClick={stopVoiceRecording}>
                                    Durdur
                                </button>
                            </div>
                        )}
                        {isUploadingVoice && <p className="uploading-hint">Ses yükleniyor...</p>}
                        {voiceRecording && !isRecording && (
                            <div className="voice-preview">
                                <audio src={voiceRecording.url} controls className="voice-preview-audio" />
                                <span className="voice-preview-name">{voiceRecording.fileName}</span>
                                <button
                                    type="button"
                                    className="file-upload-delete file-upload-delete--list"
                                    onClick={removeVoiceRecording}
                                    aria-label="Ses kaydını kaldır"
                                >
                                    <FaTrash />
                                </button>
                            </div>
                        )}
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
