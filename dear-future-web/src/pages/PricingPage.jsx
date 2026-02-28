import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getPlans } from '../api/subscription';
import AnimatedContent from '../components/AnimatedContent';
import { useDebouncedCallback } from '../hooks/useDebouncedCallback';
import './PricingPage.css';

const YEARLY_DISCOUNT = 0.2; // %20 indirim yıllık ödemede

const PricingPage = () => {
    const navigate = useNavigate();
    const [plans, setPlans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);
    const [billingPeriod, setBillingPeriod] = useState('monthly'); // 'monthly' | 'yearly'

    useEffect(() => {
        let cancelled = false;
        const load = async () => {
            try {
                const data = await getPlans();
                if (!cancelled) {
                    setPlans(Array.isArray(data) ? data : []);
                }
            } catch (err) {
                if (!cancelled) {
                    setError(true);
                    toast.error('Fiyatlar yüklenemedi.');
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };
        load();
        return () => { cancelled = true; };
    }, []);

    const handleSelectPlan = (planId) => {
        if (planId === 'FREE') {
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
                    <p>Fiyatlar yükleniyor...</p>
                </div>
            </div>
        );
    }

    if (error || plans.length === 0) {
        return (
            <div className="pricing-container">
                <div className="pricing-error">
                    <p>Fiyatlar şu an gösterilemiyor.</p>
                    <Link to="/welcome" className="pricing-back-link">Ana sayfaya dön</Link>
                </div>
            </div>
        );
    }

    const getDisplayPrice = (plan) => {
        if (plan.price === 0) return { amount: null, label: null, isYearly: false };
        if (billingPeriod === 'yearly') {
            const yearlyTotal = plan.price * 12 * (1 - YEARLY_DISCOUNT);
            return {
                amount: Math.round(yearlyTotal),
                label: plan.priceLabel?.replace(/\/ay|\/aylık|aylık/gi, '')?.trim() ? `${plan.priceLabel.replace(/\/ay|\/aylık|aylık/gi, '').trim()}/yıl` : '₺/yıl',
                isYearly: true,
                monthlyEquivalent: plan.price,
            };
        }
        return { amount: plan.price, label: plan.priceLabel || '₺/ay', isYearly: false };
    };

    return (
        <div className="pricing-container">
            <header className="pricing-header">
                <h1>Fiyatlandırma</h1>
                <p>Geleceğe mektup göndermek için size uygun planı seçin.</p>
                <div className="pricing-billing-toggle">
                    <button
                        type="button"
                        className={`pricing-toggle-btn ${billingPeriod === 'monthly' ? 'active' : ''}`}
                        onClick={() => setBillingPeriod('monthly')}
                    >
                        Aylık
                    </button>
                    <button
                        type="button"
                        className={`pricing-toggle-btn ${billingPeriod === 'yearly' ? 'active' : ''}`}
                        onClick={() => setBillingPeriod('yearly')}
                    >
                        Yıllık
                    </button>
                </div>
            </header>

            <div className="pricing-cards">
                {plans.map((plan, index) => {
                    const display = getDisplayPrice(plan);
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
                        <div className={`pricing-card ${plan.recommended ? 'recommended' : ''}`}>
                            {plan.recommended && <span className="pricing-badge">Önerilen</span>}
                            {display.isYearly && (
                                <span className="pricing-yearly-badge">Yıllık </span>
                            )}
                            <h2 className="pricing-plan-name">{plan.name}</h2>
                            {plan.description && (
                                <p className="pricing-plan-description">{plan.description}</p>
                            )}
                            <div className="pricing-price-block">
                                <span className="pricing-price">
                                    {display.amount == null ? 'Ücretsiz' : display.amount}
                                </span>
                                {display.label && <span className="pricing-price-label">{display.label}</span>}
                                {display.isYearly && display.amount != null && display.amount > 0 && (
                                    <span className="pricing-price-note">
                                        Ayda ~{Math.round(display.amount / 12)} ₺
                                    </span>
                                )}
                            </div>
                            <ul className="pricing-features">
                                {plan.features?.map((feature, i) => (
                                    <li key={i}>{feature}</li>
                                ))}
                            </ul>
                            <div className="pricing-card-actions">
                                <Link
                                    to={`/pricing/plan/${plan.id}`}
                                    className="pricing-detail-link"
                                >
                                    Detay
                                </Link>
                                <button
                                    type="button"
                                    className="pricing-cta"
                                    onClick={() => debouncedSelectPlan(plan.id)}
                                >
                                    {plan.id === 'FREE' ? 'Ücretsiz Başla' : 'Planı Seç'}
                                </button>
                            </div>
                        </div>
                    </AnimatedContent>
                    );
                })}
            </div>
        </div>
    );
};

export default PricingPage;
