import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Link } from 'react-router-dom';
import {
    FaCrown,
    FaEdit,
    FaSave,
    FaTimes,
    FaCamera,
    FaCheck,
    FaEnvelope,
    FaCalendarAlt,
    FaCog,
    FaStar,
} from 'react-icons/fa';
import { getProfile, updateProfile, uploadProfilePhoto, deleteProfilePhoto } from '../api/profile';
import './ProfilePage.css';

const ProfilePage = () => {
    const navigate = useNavigate();
    const fileInputRef = useRef(null);
    const [profile, setProfile] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [photoLoading, setPhotoLoading] = useState(false);
    const [formData, setFormData] = useState({ firstName: '', lastName: '' });

    useEffect(() => {
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        setIsLoading(true);
        try {
            const res = await getProfile();
            const user = res.data;
            setProfile(user);
            setFormData({
                firstName: user?.firstName ?? '',
                lastName: user?.lastName ?? '',
            });
        } catch (err) {
            console.error(err);
            toast.error('Profil yüklenemedi. Lütfen tekrar giriş yapın.');
            localStorage.removeItem('token');
            navigate('/login', { replace: true });
        } finally {
            setIsLoading(false);
        }
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        try {
            await updateProfile({ firstName: formData.firstName, lastName: formData.lastName });
            toast.success('Profil güncellendi.');
            setIsEditing(false);
            fetchProfile();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Profil güncellenemedi.');
        }
    };

    const handlePhotoSelect = async (e) => {
        const file = e.target?.files?.[0];
        if (!file) return;
        if (!file.type.startsWith('image/')) {
            toast.error('Sadece görsel dosyası seçin.');
            return;
        }
        setPhotoLoading(true);
        try {
            await uploadProfilePhoto(file);
            toast.success('Profil fotoğrafı güncellendi.');
            fetchProfile();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Fotoğraf yüklenemedi.');
        } finally {
            setPhotoLoading(false);
            e.target.value = '';
        }
    };

    const handleDeletePhoto = async () => {
        if (!window.confirm('Profil fotoğrafını kaldırmak istediğinize emin misiniz?')) return;
        setPhotoLoading(true);
        try {
            await deleteProfilePhoto();
            toast.success('Profil fotoğrafı kaldırıldı.');
            fetchProfile();
        } catch (err) {
            toast.error('Fotoğraf kaldırılamadı.');
        } finally {
            setPhotoLoading(false);
        }
    };

    const formatDate = (d) =>
        d ? new Date(d).toLocaleDateString('tr-TR', { year: 'numeric', month: 'long', day: 'numeric' }) : '—';

    if (isLoading) {
        return (
            <div className="profile-page">
                <div className="profile-loading">
                    <div className="spinner" />
                    <p>Profil yükleniyor...</p>
                </div>
            </div>
        );
    }

    if (!profile) {
        return (
            <div className="profile-page">
                <div className="profile-error">Profil bulunamadı.</div>
            </div>
        );
    }

    const photoUrl = profile.profilePictureUrl;
    const initials = [formData.firstName?.[0], formData.lastName?.[0]].filter(Boolean).join('').toUpperCase() || '?';
    const effectivePlan = profile.subscriptionEndsAt && new Date(profile.subscriptionEndsAt) < new Date()
        ? 'FREE'
        : (profile.subscriptionPlan || 'FREE');

    return (
        <div className="profile-page">
            <header className="profile-page-header">
                <h1 className="page-title">Profilim</h1>
                <p className="page-subtitle">Hesap bilgilerinizi, aboneliğinizi ve kişisel verilerinizi buradan yönetin.</p>
            </header>

            <div className="profile-layout">
                {/* Sol: Profil özeti + Abonelik */}
                <aside className="profile-sidebar">
                    <div className="profile-card profile-card-avatar">
                        <h3 className="section-title">Profil bilgileri</h3>
                        <p className="section-subtitle">Fotoğraf ve hesap özeti</p>
                        <div className="avatar-wrap">
                            {photoUrl ? (
                                <img src={photoUrl} alt="Profil" className="avatar-img" />
                            ) : (
                                <div className="avatar-initials">{initials}</div>
                            )}
                            <button
                                type="button"
                                className="avatar-upload-btn"
                                onClick={() => fileInputRef.current?.click()}
                                disabled={photoLoading}
                                title="Fotoğraf yükle"
                            >
                                <FaCamera />
                            </button>
                            <input
                                ref={fileInputRef}
                                type="file"
                                accept="image/*"
                                className="hidden-input"
                                onChange={handlePhotoSelect}
                            />
                        </div>
                        {photoUrl && (
                            <button type="button" className="avatar-remove-btn" onClick={handleDeletePhoto} disabled={photoLoading}>
                                Fotoğrafı kaldır
                            </button>
                        )}
                        <h2 className="profile-name">
                            {profile.firstName || profile.lastName
                                ? `${profile.firstName || ''} ${profile.lastName || ''}`.trim()
                                : 'İsimsiz Kullanıcı'}
                        </h2>
                        <p className="profile-email">
                            <FaEnvelope /> {profile.email}
                        </p>
                        {profile.emailVerified && (
                            <span className="verified-badge"><FaCheck /> Doğrulanmış</span>
                        )}
                    </div>

                    <div className="profile-card profile-card-plan">
                        <h3 className="section-title"><FaCrown /> Abonelik</h3>
                        <p className="section-subtitle">Planınız ve mesaj limitleri</p>
                        <p className="plan-name">{effectivePlan}</p>
                        {profile.subscriptionEndsAt && effectivePlan !== 'FREE' && (
                            <p className="plan-ends">Bitiş: {formatDate(profile.subscriptionEndsAt)}</p>
                        )}
                        <p className="plan-limit">Limit: {profile.maxMessagesPerPlan ?? 0} mesaj</p>
                        <button type="button" className="btn-secondary" onClick={() => navigate('/change-subscription')}>
                            Aboneliği yönet
                        </button>
                    </div>

                    <div className="profile-quick-links">
                        <Link to="/public-messages?tab=starred" className="quick-link">
                            <FaStar /> Yıldızlı mesajlarım
                        </Link>
                        <Link to="/settings" className="quick-link">
                            <FaCog /> Ayarlar ve güvenlik
                        </Link>
                    </div>
                </aside>

                {/* Sağ: Kişisel bilgiler */}
                <main className="profile-main">
                    <div className="profile-card content-card">
                        <div className="card-head">
                            <div className="card-head-text">
                                <h3>Kişisel bilgiler</h3>
                                <p className="card-head-subtitle">Ad, soyad ve üyelik bilgileriniz</p>
                            </div>
                            {!isEditing ? (
                                <button type="button" className="btn-edit" onClick={() => setIsEditing(true)}>
                                    <FaEdit /> Düzenle
                                </button>
                            ) : null}
                        </div>

                        {isEditing ? (
                            <form onSubmit={handleUpdate} className="profile-form">
                                <div className="form-row">
                                    <label>Ad</label>
                                    <input
                                        type="text"
                                        value={formData.firstName}
                                        onChange={(e) => setFormData((f) => ({ ...f, firstName: e.target.value }))}
                                        placeholder="Adınız"
                                        maxLength={50}
                                    />
                                </div>
                                <div className="form-row">
                                    <label>Soyad</label>
                                    <input
                                        type="text"
                                        value={formData.lastName}
                                        onChange={(e) => setFormData((f) => ({ ...f, lastName: e.target.value }))}
                                        placeholder="Soyadınız"
                                        maxLength={50}
                                    />
                                </div>
                                <div className="form-actions">
                                    <button type="button" className="btn-outline" onClick={() => setIsEditing(false)}>
                                        <FaTimes /> İptal
                                    </button>
                                    <button type="submit" className="btn-primary">
                                        <FaSave /> Kaydet
                                    </button>
                                </div>
                            </form>
                        ) : (
                            <div className="info-list">
                                <div className="info-row">
                                    <span className="info-label">Ad</span>
                                    <span className="info-value">{profile.firstName || '—'}</span>
                                </div>
                                <div className="info-row">
                                    <span className="info-label">Soyad</span>
                                    <span className="info-value">{profile.lastName || '—'}</span>
                                </div>
                                <div className="info-row">
                                    <span className="info-label">E-posta</span>
                                    <span className="info-value">{profile.email}</span>
                                </div>
                                <div className="info-row">
                                    <span className="info-label">Üyelik tarihi</span>
                                    <span className="info-value"><FaCalendarAlt /> {formatDate(profile.createdAt)}</span>
                                </div>
                            </div>
                        )}
                    </div>
                </main>
            </div>
        </div>
    );
};

export default ProfilePage;
