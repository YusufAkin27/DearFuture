import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaTrash, FaClock, FaPlus, FaPen, FaPaperPlane, FaHourglassStart, FaExternalLinkAlt } from 'react-icons/fa';
import { getProfile, updateSettings, deleteAccount, deactivateAccount } from '../api/profile';
import { cancelSubscription } from '../api/subscription';
import { getPendingMessages, getDeliveredMessages, deleteMessage } from '../api/message';
import EditMessageModal from '../components/EditMessageModal';
import './SettingsPage.css';

const SettingsPage = () => {
    const navigate = useNavigate();
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [formData, setFormData] = useState({
        locale: 'tr',
        emailNotifications: true,
        marketingEmails: false,
    });
    const [confirmDelete, setConfirmDelete] = useState('');
    const [confirmDeactivate, setConfirmDeactivate] = useState('');
    const [confirmCancelPlan, setConfirmCancelPlan] = useState(false);
    const [deactivating, setDeactivating] = useState(false);
    const [msgTab, setMsgTab] = useState('pending');
    const [messages, setMessages] = useState([]);
    const [messagesLoading, setMessagesLoading] = useState(false);
    const [editingMessage, setEditingMessage] = useState(null);

    useEffect(() => {
        loadProfile();
    }, []);

    useEffect(() => {
        if (profile) loadMessages();
    }, [profile, msgTab]);

    const loadMessages = async () => {
        setMessagesLoading(true);
        try {
            const res = msgTab === 'pending' ? await getPendingMessages() : await getDeliveredMessages();
            setMessages(Array.isArray(res.data) ? res.data : []);
        } catch (err) {
            console.error(err);
            setMessages([]);
        } finally {
            setMessagesLoading(false);
        }
    };

    const handleDeleteMessage = async (id) => {
        if (!window.confirm('Bu mesajı silmek istediğinize emin misiniz?')) return;
        try {
            await deleteMessage(id);
            toast.success('Mesaj silindi.');
            loadMessages();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Mesaj silinemedi.');
        }
    };

    const loadProfile = async () => {
        setLoading(true);
        try {
            const res = await getProfile();
            const user = res.data;
            setProfile(user);
            setFormData({
                locale: user.locale || 'tr',
                emailNotifications: user.emailNotifications !== false,
                marketingEmails: user.marketingEmails === true,
            });
        } catch (err) {
            toast.error('Ayarlar yüklenemedi. Lütfen tekrar giriş yapın.');
            localStorage.removeItem('token');
            navigate('/login', { replace: true });
        } finally {
            setLoading(false);
        }
    };

    const handleSettingsChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value,
        }));
    };

    const handleSaveSettings = async (e) => {
        e.preventDefault();
        setSaving(true);
        try {
            await updateSettings(formData);
            toast.success('Ayarlar kaydedildi.');
            loadProfile();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Ayarlar kaydedilemedi.');
        } finally {
            setSaving(false);
        }
    };

    const planCode = profile?.subscriptionPlanCode ?? profile?.subscriptionPlan ?? 'FREE';
    const planName = profile?.subscriptionPlanName ?? planCode;
    const effectivePlan = profile?.subscriptionEndsAt && new Date(profile.subscriptionEndsAt) < new Date()
        ? 'FREE'
        : (profile?.subscriptionPlan || 'FREE');

    const hasActiveSubscription = () => {
        if (!planCode || planCode === 'FREE') return false;
        if (!profile?.subscriptionEndsAt) return true;
        return new Date(profile.subscriptionEndsAt) > new Date();
    };

    const handleDeactivateAccount = async () => {
        if (confirmDeactivate !== 'DONDUR') {
            toast.error('Onaylamak için "DONDUR" yazın.');
            return;
        }
        setDeactivating(true);
        try {
            await deactivateAccount();
            toast.success('Hesabınız donduruldu. Giriş yapamazsınız.');
            localStorage.removeItem('token');
            navigate('/login', { replace: true });
        } catch (err) {
            toast.error(err.response?.data?.message || 'Hesap dondurulamadı.');
        } finally {
            setDeactivating(false);
        }
    };

    const handleCancelSubscription = async () => {
        if (!confirmCancelPlan) {
            setConfirmCancelPlan(true);
            return;
        }
        try {
            await cancelSubscription();
            toast.success('Abonelik iptal edildi.');
            setConfirmCancelPlan(false);
            loadProfile();
        } catch (err) {
            toast.error(err.response?.data?.message || 'İptal işlemi başarısız.');
        }
    };

    const handleDeleteAccount = async () => {
        if (confirmDelete !== 'SİL') {
            toast.error('Onaylamak için "SİL" yazın.');
            return;
        }
        try {
            await deleteAccount();
            toast.info('Hesabınız silindi.');
            localStorage.removeItem('token');
            navigate('/login', { replace: true });
        } catch (err) {
            toast.error(err.response?.data?.message || 'Hesap silinemedi.');
        }
    };

    if (loading) {
        return (
            <div className="settings-container">
                <div className="settings-loading">
                    <div className="spinner" />
                    <p>Yükleniyor...</p>
                </div>
            </div>
        );
    }

    if (!profile) {
        return (
            <div className="settings-container">
                <div className="settings-error">Profil yüklenemedi.</div>
            </div>
        );
    }

    return (
        <div className="settings-container">
            <div className="settings-header">
                <h1>Ayarlar</h1>
                <p>Hesap tercihlerinizi ve uygulama ayarlarınızı yönetin.</p>
            </div>

            {/* Mesajlarım - Bekleyen / İletilen */}
            <div className="settings-messages-wrap">
                <div className="settings-messages-card">
                    <div className="settings-messages-head">
                        <h2>Mesajlarım</h2>
                        <Link to="/new" className="settings-new-msg-btn">
                            <FaPlus /> Yeni Mesaj
                        </Link>
                    </div>
                    <div className="settings-messages-tabs">
                        <button
                            type="button"
                            className={`settings-tab-btn ${msgTab === 'pending' ? 'active' : ''}`}
                            onClick={() => setMsgTab('pending')}
                        >
                            <FaHourglassStart /> Bekleyenler
                        </button>
                        <button
                            type="button"
                            className={`settings-tab-btn ${msgTab === 'delivered' ? 'active' : ''}`}
                            onClick={() => setMsgTab('delivered')}
                        >
                            <FaPaperPlane /> İletilenler
                        </button>
                    </div>
                    {messagesLoading ? (
                        <div className="settings-messages-loading">
                            <div className="spinner" />
                            <p>Mesajlar yükleniyor...</p>
                        </div>
                    ) : (
                        <div className="settings-message-grid">
                            {messages.length === 0 ? (
                                <div className="settings-messages-empty">
                                    <div className="settings-messages-empty-icon">
                                        {msgTab === 'pending' ? <FaHourglassStart /> : <FaPaperPlane />}
                                    </div>
                                    <h3>Burada henüz bir şey yok</h3>
                                    <p>
                                        {msgTab === 'pending'
                                            ? 'Henüz geleceğe gönderilmek üzere bekleyen bir mesajın yok.'
                                            : 'Henüz teslim edilmiş bir mesajın yok.'}
                                    </p>
                                    {msgTab === 'pending' && (
                                        <Link to="/new" className="settings-empty-cta">İlk mesajını yaz</Link>
                                    )}
                                </div>
                            ) : (
                                messages.map((msg) => (
                                    <div key={msg.id} className="settings-message-card">
                                        <div className="settings-message-card-top">
                                            <span className="settings-message-date">
                                                <FaClock />
                                                {new Date(msg.scheduledAt).toLocaleDateString('tr-TR', {
                                                    year: 'numeric',
                                                    month: 'long',
                                                    day: 'numeric',
                                                    hour: '2-digit',
                                                    minute: '2-digit',
                                                })}
                                            </span>
                                            <span className={`settings-status-badge ${msgTab}`}>
                                                {msgTab === 'pending' ? 'Bekliyor' : 'İletildi'}
                                            </span>
                                        </div>
                                        <div className="settings-message-preview">
                                            {(msg.content && msg.content.substring(0, 150)) || 'Metin içeriği yok'}
                                            {msg.content && msg.content.length > 150 && '...'}
                                        </div>
                                        <div className="settings-message-actions">
                                            {msgTab === 'pending' && effectivePlan !== 'FREE' && (
                                                <>
                                                    <button
                                                        type="button"
                                                        onClick={() => setEditingMessage(msg)}
                                                        className="settings-action-btn edit"
                                                        title="Düzenle"
                                                    >
                                                        <FaPen /> Düzenle
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() => handleDeleteMessage(msg.id)}
                                                        className="settings-action-btn delete"
                                                        title="Sil"
                                                    >
                                                        <FaTrash /> Sil
                                                    </button>
                                                </>
                                            )}
                                            {msgTab === 'pending' && effectivePlan === 'FREE' && (
                                                <span className="settings-free-hint">Ücretsiz hesapta bekleyen mesajlar düzenlenemez veya silinemez.</span>
                                            )}
                                            {msgTab === 'delivered' && msg.viewToken && (
                                                <Link
                                                    to={`/message/view/${msg.viewToken}`}
                                                    className="settings-action-btn view-link"
                                                    title="Mesajın sayfasına git"
                                                >
                                                    <FaExternalLinkAlt /> Mesajı görüntüle
                                                </Link>
                                            )}
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    )}
                </div>
            </div>

            {editingMessage && (
                <EditMessageModal
                    message={editingMessage}
                    onClose={() => setEditingMessage(null)}
                    onUpdate={loadMessages}
                />
            )}

            <div className="settings-grid">
                {/* Hesap bilgisi (salt okunur) */}
                <div className="settings-card">
                    <h2>Hesap</h2>
                    <div className="form-group">
                        <label>E-posta</label>
                        <input type="email" value={profile.email || ''} readOnly disabled />
                        <p className="help-text">E-posta değişikliği için destek ile iletişime geçin.</p>
                    </div>
                </div>

                {/* Genel: Dil */}
                <form className="settings-card" onSubmit={handleSaveSettings}>
                    <h2>Genel</h2>
                    <div className="form-group">
                        <label htmlFor="locale">Dil</label>
                        <select
                            id="locale"
                            name="locale"
                            value={formData.locale}
                            onChange={handleSettingsChange}
                        >
                            <option value="tr">Türkçe</option>
                            <option value="en">English</option>
                        </select>
                    </div>
                    <div className="form-actions">
                        <button type="submit" className="save-btn" disabled={saving}>
                            {saving ? 'Kaydediliyor...' : 'Kaydet'}
                        </button>
                    </div>
                </form>

                {/* Bildirimler */}
                <form className="settings-card" onSubmit={handleSaveSettings}>
                    <h2>Bildirimler</h2>
                    <div className="form-group checkbox-group">
                        <label className="toggle-switch">
                            <input
                                type="checkbox"
                                name="emailNotifications"
                                checked={formData.emailNotifications}
                                onChange={handleSettingsChange}
                            />
                            <span className="slider round" />
                        </label>
                        <div className="toggle-label">
                            <span>E-posta bildirimleri</span>
                            <p>Planlanmış mesajlarınız hakkında hatırlatma e-postaları alın.</p>
                        </div>
                    </div>
                    <div className="form-group checkbox-group">
                        <label className="toggle-switch">
                            <input
                                type="checkbox"
                                name="marketingEmails"
                                checked={formData.marketingEmails}
                                onChange={handleSettingsChange}
                            />
                            <span className="slider round" />
                        </label>
                        <div className="toggle-label">
                            <span>Pazarlama e-postaları</span>
                            <p>Kampanya ve yenilikler hakkında bilgi alın.</p>
                        </div>
                    </div>
                    <div className="form-actions">
                        <button type="submit" className="save-btn" disabled={saving}>
                            {saving ? 'Kaydediliyor...' : 'Kaydet'}
                        </button>
                    </div>
                </form>

                {/* Abonelik */}
                <div className="settings-card">
                    <h2>Abonelik</h2>
                    <p className="settings-plan-info">
                        Mevcut plan: <strong>{planName}</strong>
                        {profile.subscriptionEndsAt && (
                            <> · Bitiş: {new Date(profile.subscriptionEndsAt).toLocaleDateString('tr-TR')}</>
                        )}
                    </p>
                    {hasActiveSubscription() ? (
                        <div className="subscription-cancel-block">
                            {confirmCancelPlan ? (
                                <>
                                    <p className="confirm-text">Aboneliği iptal etmek istediğinize emin misiniz? Plan FREE olacaktır.</p>
                                    <div className="form-actions">
                                        <button type="button" className="secondary-btn" onClick={() => setConfirmCancelPlan(false)}>
                                            Vazgeç
                                        </button>
                                        <button type="button" className="cancel-plan-btn" onClick={handleCancelSubscription}>
                                            İptal et
                                        </button>
                                    </div>
                                </>
                            ) : (
                                <button type="button" className="secondary-btn" onClick={handleCancelSubscription}>
                                    Aboneliği iptal et
                                </button>
                            )}
                        </div>
                    ) : (
                        <p className="help-text">Yükseltmek için <Link to="/change-subscription">Aboneliği yönet</Link> sayfasına gidin.</p>
                    )}
                </div>

                {/* Hesap dondurma */}
                <div className="settings-card danger-zone danger-zone-warn">
                    <div className="danger-zone-header">
                        <h2>Hesabı dondur</h2>
                        <p className="danger-zone-desc">Hesabınız devre dışı bırakılır, giriş yapamazsınız. Verileriniz silinmez. Yeniden açmak için destek ile iletişime geçin.</p>
                    </div>
                    <div className="danger-actions">
                        <div className="danger-block">
                            <label id="deactivate-hint" className="confirm-delete-label">Onaylamak için <strong>DONDUR</strong> yazın</label>
                            <input
                                type="text"
                                className="confirm-delete-input"
                                placeholder="DONDUR"
                                value={confirmDeactivate}
                                onChange={(e) => setConfirmDeactivate(e.target.value.toUpperCase())}
                                aria-describedby="deactivate-hint"
                            />
                            <button
                                type="button"
                                className="deactivate-btn"
                                onClick={handleDeactivateAccount}
                                disabled={confirmDeactivate !== 'DONDUR' || deactivating}
                            >
                                {deactivating ? 'İşleniyor...' : 'Hesabı dondur'}
                            </button>
                        </div>
                    </div>
                </div>

                {/* Hesap silme */}
                <div className="settings-card danger-zone">
                    <div className="danger-zone-header">
                        <h2>Hesap silme</h2>
                        <p className="danger-zone-desc">Tüm verileriniz kalıcı olarak silinir. Bu işlem geri alınamaz.</p>
                    </div>

                    <div className="danger-actions">
                        <div className="danger-block danger-block-delete">
                            <h3 className="danger-block-title">Hesabı kalıcı sil</h3>
                            <label id="delete-hint" className="confirm-delete-label">Kalıcı silmek için <strong>SİL</strong> yazın</label>
                            <input
                                type="text"
                                className="confirm-delete-input"
                                placeholder="SİL"
                                value={confirmDelete}
                                onChange={(e) => setConfirmDelete(e.target.value.toUpperCase())}
                                aria-describedby="delete-hint"
                            />
                            <button
                                type="button"
                                className="delete-btn"
                                onClick={handleDeleteAccount}
                                disabled={confirmDelete !== 'SİL'}
                            >
                                Hesabı kalıcı sil
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SettingsPage;
