import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getProfile } from '../api/profile';
import { initializeCheckout } from '../api/subscription';
import './ChangeSubscriptionPage.css';

const PLANS = [
    {
        id: 'FREE',
        name: 'Ücretsiz',
        price: '0',
        priceLabel: '₺/ay',
        features: ['3 zamanlanmış mesaj', 'Sadece metin', '1 alıcı / mesaj'],
        recommended: false,
    },
    {
        id: 'PLUS',
        name: 'Plus',
        price: '100',
        priceLabel: '₺/ay',
        features: ['20 zamanlanmış mesaj', 'Fotoğraf & dosya', '5 alıcı / mesaj', 'Öncelikli özellikler'],
        recommended: true,
    },
    {
        id: 'PREMIUM',
        name: 'Premium',
        price: '150',
        priceLabel: '₺/ay',
        features: ['100 zamanlanmış mesaj', 'Fotoğraf, dosya & ses kaydı', '20 alıcı / mesaj', 'Tüm özellikler'],
        recommended: false,
    },
];

const ChangeSubscriptionPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [payLoading, setPayLoading] = useState(null);

    useEffect(() => {
        fetchProfile();
    }, []);

    useEffect(() => {
        const success = searchParams.get('success');
        const message = searchParams.get('message');
        if (success === '1') {
            toast.success('Ödeme başarılı! Aboneliğiniz aktif.');
            fetchProfile();
            window.history.replaceState({}, '', '/change-subscription');
        } else if (success === '0') {
            toast.error(message ? decodeURIComponent(message) : 'Ödeme tamamlanamadı.');
            window.history.replaceState({}, '', '/change-subscription');
        }
    }, [searchParams]);

    const fetchProfile = async () => {
        setLoading(true);
        try {
            const res = await getProfile();
            setProfile(res.data);
        } catch (err) {
            console.error(err);
            toast.error('Profil yüklenemedi. Lütfen tekrar giriş yapın.');
            localStorage.removeItem('token');
            navigate('/login', { replace: true });
        } finally {
            setLoading(false);
        }
    };

    const currentPlan = profile?.subscriptionPlan || 'FREE';
    const subscriptionEndsAt = profile?.subscriptionEndsAt;
    const isExpired = subscriptionEndsAt && new Date(subscriptionEndsAt) < new Date();
    const effectivePlan = isExpired ? 'FREE' : currentPlan;

    const handleUpgrade = async (planId) => {
        if (planId === 'FREE') return;
        setPayLoading(planId);
        try {
            const data = await initializeCheckout(planId);
            if (data.paymentPageUrl) {
                window.location.href = data.paymentPageUrl;
                return;
            }
            toast.error('Ödeme sayfası alınamadı.');
        } catch (err) {
            console.error(err);
            const msg = err.response?.data?.message || 'Ödeme başlatılamadı. Tekrar deneyin.';
            toast.error(msg);
        } finally {
            setPayLoading(null);
        }
    };

    const formatDate = (dateStr) => {
        if (!dateStr) return null;
        return new Date(dateStr).toLocaleDateString('tr-TR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
        });
    };

    if (loading) {
        return (
            <div className="subscription-container">
                <div className="subscription-loading">Yükleniyor...</div>
            </div>
        );
    }

    return (
        <div className="subscription-container">
            <div className="subscription-header">
                <h1>Aboneliği Yönet</h1>
                <p>
                    Mevcut plan:{' '}
                    <span className="current-plan-badge">{effectivePlan}</span>
                    {subscriptionEndsAt && !isExpired && (
                        <span className="subscription-ends">
                            {' '}({formatDate(subscriptionEndsAt)} tarihine kadar)
                        </span>
                    )}
                </p>
                {profile?.maxMessagesPerPlan != null && (
                    <p className="plan-limit-info">
                        Plan limiti: {profile.maxMessagesPerPlan} zamanlanmış mesaj
                    </p>
                )}
            </div>

            <div className="subscription-cards">
                {PLANS.map((plan) => {
                    const isCurrent = effectivePlan === plan.id;
                    const canUpgrade = plan.id !== 'FREE' && !isCurrent;
                    return (
                        <div
                            key={plan.id}
                            className={`subscription-card ${isCurrent ? 'current' : ''}`}
                        >
                            {isCurrent && <div className="badge current">Mevcut Plan</div>}
                            {plan.recommended && !isCurrent && <div className="badge">Önerilen</div>}

                            <h2>{plan.name}</h2>
                            <div className="price">
                                {plan.price} <span className="price-label">{plan.priceLabel}</span>
                            </div>

                            <ul className="features">
                                {plan.features.map((feature, index) => (
                                    <li key={index}>{feature}</li>
                                ))}
                            </ul>

                            <button
                                className={`action-btn ${isCurrent ? 'disabled' : 'upgrade'}`}
                                onClick={() => handleUpgrade(plan.id)}
                                disabled={isCurrent || payLoading !== null}
                            >
                                {isCurrent
                                    ? 'Aktif'
                                    : plan.id === 'FREE'
                                    ? 'Ücretsiz'
                                    : payLoading === plan.id
                                    ? 'Yönlendiriliyor...'
                                    : 'Satın Al'}
                            </button>
                        </div>
                    );
                })}
            </div>

            <div className="subscription-info">
                <p>
                    Ödeme iyzico güvencesiyle alınır. Abonelik her ay otomatik yenilenir; süre sonunda
                    yenileme yapılmazsa plan Ücretsiz’e döner.
                </p>
            </div>
        </div>
    );
};

export default ChangeSubscriptionPage;
