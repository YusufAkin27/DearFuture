import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getPlans } from '../api/subscription';
import './PricingPage.css';

const PricingPage = () => {
    const navigate = useNavigate();
    const [plans, setPlans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);

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

    return (
        <div className="pricing-container">
            <header className="pricing-header">
                <h1>Fiyatlandırma</h1>
                <p>Geleceğe mektup göndermek için size uygun planı seçin.</p>
            </header>

            <div className="pricing-cards">
                {plans.map((plan) => (
                    <div key={plan.id} className={`pricing-card ${plan.recommended ? 'recommended' : ''}`}>
                        {plan.recommended && <span className="pricing-badge">Önerilen</span>}
                        <h2 className="pricing-plan-name">{plan.name}</h2>
                        <div className="pricing-price-block">
                            <span className="pricing-price">{plan.price === 0 ? 'Ücretsiz' : plan.price}</span>
                            {plan.price > 0 && <span className="pricing-price-label">{plan.priceLabel}</span>}
                        </div>
                        <ul className="pricing-features">
                            {plan.features?.map((feature, index) => (
                                <li key={index}>{feature}</li>
                            ))}
                        </ul>
                        <button
                            type="button"
                            className="pricing-cta"
                            onClick={() => handleSelectPlan(plan.id)}
                        >
                            {plan.id === 'FREE' ? 'Ücretsiz Başla' : 'Planı Seç'}
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default PricingPage;
