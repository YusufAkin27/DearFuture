import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaPaperPlane, FaExclamationTriangle } from 'react-icons/fa';
import { getProfile, updateSettings } from '../api/profile';
import { cancelSubscription } from '../api/subscription';
import { useDebouncedCallback } from '../hooks/useDebouncedCallback';
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
    const [confirmCancelPlan, setConfirmCancelPlan] = useState(false);
    /** Hata ayıklama: profil yüklenirken oluşan son hata detayı (sayfada gösterilir) */
    const [loadErrorDetail, setLoadErrorDetail] = useState(null);

    useEffect(() => {
        loadProfile();
    }, []);

    const loadProfile = async () => {
        setLoading(true);
        setLoadErrorDetail(null);
        try {
            const res = await getProfile();
            const user = res.data;
            if (!user) {
                toast.error('Profil yanıtı alınamadı.');
                setLoadErrorDetail('Backend boş yanıt döndü.');
                setLoading(false);
                return;
            }
            setProfile(user);
            setFormData({
                locale: user.locale || 'tr',
                emailNotifications: user.emailNotifications !== false,
                marketingEmails: user.marketingEmails === true,
            });
        } catch (err) {
            const status = err.response?.status;
            const url = err.config?.baseURL + (err.config?.url || '');
            const msg = err.response?.data?.message || err.message || 'Bilinmeyen hata';
            const detail = `Status: ${status ?? 'yok'} | URL: ${url || 'bilinmiyor'} | Mesaj: ${msg}`;
            setLoadErrorDetail(detail);
            console.error('[SettingsPage] getProfile hatası:', detail, err.response?.data, err);
            const isAuthError = status === 401 || status === 403;
            toast.error(
                isAuthError
                    ? 'Oturum süresi doldu. Lütfen tekrar giriş yapın.'
                    : (err.response?.data?.message || 'Ayarlar yüklenemedi. Sayfayı yenileyip tekrar deneyin.')
            );
            if (isAuthError) {
                localStorage.removeItem('token');
                navigate('/login', { replace: true });
            }
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

    const hasActiveSubscription = () => {
        if (!planCode || planCode === 'FREE') return false;
        if (!profile?.subscriptionEndsAt) return true;
        return new Date(profile.subscriptionEndsAt) > new Date();
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

    const debouncedSaveSettings = useDebouncedCallback(handleSaveSettings, 500);
    const debouncedCancelSubscription = useDebouncedCallback(handleCancelSubscription, 500);

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
                {loadErrorDetail && (
                    <pre className="app-debug-log" aria-live="polite">
                        {loadErrorDetail}
                    </pre>
                )}
            </div>
        );
    }

    return (
        <div className="settings-container">
            <div className="settings-header">
                <h1>Ayarlar</h1>
                <p>Hesap tercihlerinizi ve uygulama ayarlarınızı yönetin.</p>
            </div>

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
                <form className="settings-card" onSubmit={debouncedSaveSettings}>
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
                <form className="settings-card" onSubmit={debouncedSaveSettings}>
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
                                        <button type="button" className="cancel-plan-btn" onClick={debouncedCancelSubscription}>
                                            İptal et
                                        </button>
                                    </div>
                                </>
                            ) : (
                                <button type="button" className="secondary-btn" onClick={debouncedCancelSubscription}>
                                    Aboneliği iptal et
                                </button>
                            )}
                        </div>
                    ) : (
                        <p className="help-text">Yükseltmek için <Link to="/change-subscription">Aboneliği yönet</Link> sayfasına gidin.</p>
                    )}
                </div>

                {/* İletilen mesajlar – ayrı sayfaya yönlendir */}
                <Link to="/settings/delivered-messages" className="settings-card settings-link-card">
                    <span className="settings-link-card-icon"><FaPaperPlane /></span>
                    <h2>İletilen mesajlar</h2>
                    <p className="settings-link-card-desc">Teslim edilmiş mesajlarınızı görüntüleyin.</p>
                    <span className="settings-link-card-arrow">→</span>
                </Link>

                {/* Hesap dondurma ve silme – ayrı sayfaya yönlendir */}
                <Link to="/settings/account-danger" className="settings-card settings-link-card settings-link-card-danger">
                    <span className="settings-link-card-icon"><FaExclamationTriangle /></span>
                    <h2>Hesap dondurma ve silme</h2>
                    <p className="settings-link-card-desc">Hesabı dondurma veya kalıcı silme işlemleri.</p>
                    <span className="settings-link-card-arrow">→</span>
                </Link>
            </div>
        </div>
    );
};

export default SettingsPage;
