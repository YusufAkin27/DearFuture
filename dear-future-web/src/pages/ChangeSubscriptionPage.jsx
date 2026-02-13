import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getProfile } from '../api/profile';
import { getPlans, initializeCheckout } from '../api/subscription';
import AnimatedContent from '../components/AnimatedContent';
import './PricingPage.css';
import './ChangeSubscriptionPage.css';

const ChangeSubscriptionPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [profile, setProfile] = useState(null);
    const [plans, setPlans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [payLoading, setPayLoading] = useState(null);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            try {
                const [profileRes, plansData] = await Promise.all([
                    getProfile(),
                    getPlans()
                ]);
                setProfile(profileRes.data);
                setPlans(Array.isArray(plansData) ? plansData : []);
            } catch (err) {
                console.error(err);
                toast.error('Bilgiler yüklenemedi. Lütfen tekrar giriş yapın.');
                localStorage.removeItem('token');
                navigate('/login', { replace: true });
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [navigate]);

    useEffect(() => {
        const success = searchParams.get('success');
        const message = searchParams.get('message');
        if (success === '1') {
            toast.success('Ödeme başarılı! Aboneliğiniz aktif.');
            getProfile().then((res) => setProfile(res.data)).catch(() => {});
            window.history.replaceState({}, '', '/change-subscription');
        } else if (success === '0') {
            toast.error(message ? decodeURIComponent(message) : 'Ödeme tamamlanamadı.');
            window.history.replaceState({}, '', '/change-subscription');
        }
    }, [searchParams]);

    const currentPlan = profile?.subscriptionPlanCode || 'FREE';
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
            <div className="pricing-container">
                <div className="pricing-loading">
                    <div className="pricing-spinner" />
                    <p>Yükleniyor...</p>
                </div>
            </div>
        );
    }

    if (plans.length === 0) {
        return (
            <div className="pricing-container">
                <div className="pricing-error">
                    <p>Planlar şu an gösterilemiyor.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="pricing-container">
            <header className="pricing-header">
                <h1>Aboneliği Yönet</h1>
                <p>
                    Mevcut planınız:{' '}
                    <span className="change-sub-current-badge">{effectivePlan}</span>
                    {subscriptionEndsAt && !isExpired && (
                        <span className="change-sub-ends">
                            {' '}({formatDate(subscriptionEndsAt)} tarihine kadar)
                        </span>
                    )}
                </p>
            </header>

            <div className="pricing-cards">
                {plans.map((plan, index) => {
                    const isCurrent = effectivePlan === plan.id;
                    const canUpgrade = plan.id !== 'FREE' && !isCurrent;
                    return (
                        <AnimatedContent
                            key={plan.id}
                            distance={100}
                            direction="vertical"
                            reverse={false}
                            duration={0.8}
                            ease="power3.out"
                            initialOpacity={0}
                            animateOpacity
                            scale={1}
                            threshold={0.1}
                            delay={index * 0.15}
                        >
                            <div className={`pricing-card ${plan.recommended ? 'recommended' : ''} ${isCurrent ? 'current-plan' : ''}`}>
                                {isCurrent && <span className="pricing-badge pricing-badge--current">Mevcut Plan</span>}
                                {plan.recommended && !isCurrent && <span className="pricing-badge">Önerilen</span>}
                                <h2 className="pricing-plan-name">{plan.name}</h2>
                                {plan.description && (
                                    <p className="pricing-plan-description">{plan.description}</p>
                                )}
                                <div className="pricing-price-block">
                                    <span className="pricing-price">{plan.price === 0 ? 'Ücretsiz' : plan.price}</span>
                                    {plan.price > 0 && <span className="pricing-price-label">{plan.priceLabel}</span>}
                                </div>
                                <ul className="pricing-features">
                                    {plan.features?.map((feature, i) => (
                                        <li key={i}>{feature}</li>
                                    ))}
                                </ul>
                                <button
                                    type="button"
                                    className="pricing-cta"
                                    onClick={() => handleUpgrade(plan.id)}
                                    disabled={isCurrent || (plan.id === 'FREE' && !isCurrent) || payLoading !== null}
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
                        </AnimatedContent>
                    );
                })}
            </div>

            <div className="change-sub-info">
                <p>
                    Ödeme iyzico güvencesiyle alınır. Abonelik her ay otomatik yenilenir; süre sonunda
                    yenileme yapılmazsa plan Ücretsiz'e döner.
                </p>
            </div>
        </div>
    );
};

export default ChangeSubscriptionPage;
