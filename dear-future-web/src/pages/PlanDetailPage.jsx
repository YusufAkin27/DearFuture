import { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaArrowLeft, FaCheck, FaPaperPlane, FaUserFriends, FaImage, FaFile, FaMicrophone } from 'react-icons/fa';
import { getPlanByCode } from '../api/subscription';
import { useDebouncedCallback } from '../hooks/useDebouncedCallback';
import './PricingPage.css';
import './PlanDetailPage.css';

const formatBytes = (bytes) => {
    if (!bytes || bytes === 0) return '—';
    const mb = bytes / (1024 * 1024);
    return mb >= 1 ? `${Math.round(mb)} MB` : `${Math.round(bytes / 1024)} KB`;
};

const PlanDetailPage = () => {
    const { code } = useParams();
    const navigate = useNavigate();
    const [plan, setPlan] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);

    useEffect(() => {
        let cancelled = false;
        if (!code) {
            setError(true);
            setLoading(false);
            return;
        }
        const load = async () => {
            try {
                const data = await getPlanByCode(code);
                if (!cancelled) setPlan(data);
            } catch (err) {
                if (!cancelled) {
                    setError(true);
                    toast.error('Plan bilgisi yüklenemedi.');
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };
        load();
        return () => { cancelled = true; };
    }, [code]);

    const handleSelectPlan = () => {
        if (plan?.id === 'FREE') {
            navigate('/login');
            return;
        }
        const token = localStorage.getItem('token');
        if (token) {
            navigate('/change-subscription');
        } else {
            navigate('/login');
        }
    };

    const debouncedSelectPlan = useDebouncedCallback(handleSelectPlan, 500);

    if (loading) {
        return (
            <div className="pricing-container">
                <div className="pricing-loading">
                    <div className="pricing-spinner" />
                    <p>Plan yükleniyor...</p>
                </div>
            </div>
        );
    }

    if (error || !plan) {
        return (
            <div className="pricing-container">
                <div className="pricing-error">
                    <p>Plan bulunamadı.</p>
                    <Link to="/pricing" className="pricing-back-link">Fiyatlandırmaya dön</Link>
                </div>
            </div>
        );
    }

    return (
        <div className="pricing-container plan-detail-container">
            <Link to="/pricing" className="plan-detail-back">
                <FaArrowLeft aria-hidden /> Fiyatlandırmaya dön
            </Link>

            <header className="plan-detail-hero">
                <div className="plan-detail-hero-inner">
                    {plan.recommended && <span className="plan-detail-badge">Önerilen</span>}
                    <span className="plan-detail-code">{plan.id}</span>
                    <h1 className="plan-detail-title">{plan.name}</h1>
                    {plan.description && (
                        <p className="plan-detail-desc">{plan.description}</p>
                    )}
                    <div className="plan-detail-price-wrap">
                        <span className="plan-detail-price-main">
                            {plan.price === 0 ? 'Ücretsiz' : `${plan.price}`}
                        </span>
                        {plan.price > 0 && (
                            <span className="plan-detail-price-unit">{plan.priceLabel}</span>
                        )}
                    </div>
                </div>
            </header>

            {plan.features && plan.features.length > 0 && (
                <section className="plan-detail-block plan-detail-features">
                    <h2 className="plan-detail-block-title">Neler dahil?</h2>
                    <ul className="plan-detail-feature-list">
                        {plan.features.map((feature, i) => (
                            <li key={i}>
                                <span className="plan-detail-feature-icon"><FaCheck /></span>
                                <span>{feature}</span>
                            </li>
                        ))}
                    </ul>
                </section>
            )}

            <section className="plan-detail-block plan-detail-limits">
                <h2 className="plan-detail-block-title">Limitler ve kotalar</h2>
                <div className="plan-detail-limit-grid">
                    <div className="plan-detail-limit-card">
                        <span className="plan-detail-limit-icon"><FaPaperPlane /></span>
                        <div>
                            <span className="plan-detail-limit-label">Mesaj hakkı</span>
                            <span className="plan-detail-limit-value">
                                {plan.maxMessages === 0 ? '—' : `${plan.maxMessages} mesaj`}
                            </span>
                        </div>
                    </div>
                    <div className="plan-detail-limit-card">
                        <span className="plan-detail-limit-icon"><FaUserFriends /></span>
                        <div>
                            <span className="plan-detail-limit-label">Alıcı (mesaj başına)</span>
                            <span className="plan-detail-limit-value">{plan.maxRecipientsPerMessage} kişi</span>
                        </div>
                    </div>
                    <div className="plan-detail-limit-card">
                        <span className={`plan-detail-limit-icon ${plan.allowPhoto ? 'enabled' : ''}`}><FaImage /></span>
                        <div>
                            <span className="plan-detail-limit-label">Fotoğraf / video</span>
                            <span className="plan-detail-limit-value">
                                {plan.allowPhoto
                                    ? `${plan.maxPhotosPerMessage} adet, ${formatBytes(plan.maxPhotoSizeBytes)}`
                                    : 'Yok'}
                            </span>
                        </div>
                    </div>
                    <div className="plan-detail-limit-card">
                        <span className={`plan-detail-limit-icon ${plan.allowFile ? 'enabled' : ''}`}><FaFile /></span>
                        <div>
                            <span className="plan-detail-limit-label">Dosya</span>
                            <span className="plan-detail-limit-value">
                                {plan.allowFile
                                    ? `${plan.maxFilesPerMessage} dosya, ${formatBytes(plan.maxFileSizeBytes)}`
                                    : 'Yok'}
                            </span>
                        </div>
                    </div>
                    <div className="plan-detail-limit-card">
                        <span className={`plan-detail-limit-icon ${plan.allowVoice ? 'enabled' : ''}`}><FaMicrophone /></span>
                        <div>
                            <span className="plan-detail-limit-label">Ses kaydı</span>
                            <span className="plan-detail-limit-value">
                                {plan.allowVoice
                                    ? `${plan.maxAudioPerMessage} ses, ${formatBytes(plan.maxAudioSizeBytes)}`
                                    : 'Yok'}
                            </span>
                        </div>
                    </div>
                </div>
                {!plan.active && (
                    <p className="plan-detail-inactive-note">
                        Bu plan şu an yeni abonelik için satın alınamıyor.
                    </p>
                )}
            </section>

            <div className="plan-detail-actions">
                <button
                    type="button"
                    className={`plan-detail-cta ${plan.recommended ? 'plan-detail-cta--primary' : 'plan-detail-cta--secondary'}`}
                    onClick={debouncedSelectPlan}
                    disabled={!plan.active && plan.id !== 'FREE'}
                >
                    {plan.id === 'FREE' ? 'Ücretsiz Başla' : plan.active ? 'Planı Seç' : 'Satın alınamıyor'}
                </button>
            </div>
        </div>
    );
};

export default PlanDetailPage;
