import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getProfile, updateSettings, deleteAccount, deactivateAccount } from '../api/profile';
import { cancelSubscription } from '../api/subscription';
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
    const [confirmDeactivate, setConfirmDeactivate] = useState(false);
    const [confirmCancelPlan, setConfirmCancelPlan] = useState(false);

    useEffect(() => {
        loadProfile();
    }, []);

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

    const hasActiveSubscription = () => {
        if (!profile?.subscriptionPlan || profile.subscriptionPlan === 'FREE') return false;
        if (!profile.subscriptionEndsAt) return true;
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

    const handleDeactivate = async () => {
        if (!confirmDeactivate) {
            setConfirmDeactivate(true);
            return;
        }
        try {
            await deactivateAccount();
            toast.info('Hesabınız devre dışı bırakıldı.');
            localStorage.removeItem('token');
            navigate('/login', { replace: true });
        } catch (err) {
            toast.error(err.response?.data?.message || 'İşlem başarısız.');
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
                        Mevcut plan: <strong>{profile.subscriptionPlan || 'FREE'}</strong>
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
                        <p className="help-text">Yükseltmek için <a href="/change-subscription">Aboneliği yönet</a> sayfasına gidin.</p>
                    )}
                </div>

                {/* Tehlikeli bölge */}
                <div className="settings-card danger-zone">
                    <h2>Tehlikeli bölge</h2>
                    <p className="danger-desc">Hesabı devre dışı bırakırsanız giriş yapamazsınız; verileriniz saklanır. Hesabı silerseniz tüm verileriniz kalıcı olarak silinir.</p>

                    <div className="danger-actions">
                        <div className="danger-item">
                            {confirmDeactivate ? (
                                <>
                                    <p className="confirm-text">Hesabınız devre dışı bırakılacak. Emin misiniz?</p>
                                    <div className="form-actions">
                                        <button type="button" className="secondary-btn" onClick={() => setConfirmDeactivate(false)}>Vazgeç</button>
                                        <button type="button" className="deactivate-btn" onClick={handleDeactivate}>Devre dışı bırak</button>
                                    </div>
                                </>
                            ) : (
                                <button type="button" className="deactivate-btn" onClick={handleDeactivate}>
                                    Hesabı devre dışı bırak
                                </button>
                            )}
                        </div>

                        <div className="danger-item">
                            <p className="help-text">Kalıcı silmek için aşağıya <strong>SİL</strong> yazın.</p>
                            <input
                                type="text"
                                className="confirm-delete-input"
                                placeholder="SİL yazın"
                                value={confirmDelete}
                                onChange={(e) => setConfirmDelete(e.target.value.toUpperCase())}
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
