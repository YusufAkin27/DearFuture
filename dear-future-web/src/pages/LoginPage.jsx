import { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaEnvelope, FaPaperPlane, FaLock, FaArrowLeft, FaGoogle } from 'react-icons/fa';
import { login } from '../api/auth';
import { useDebouncedCallback } from '../hooks/useDebouncedCallback';
import './LoginPage.css';

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [isFocused, setIsFocused] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();
    const returnTo = location.state?.from || '/';

    useEffect(() => {
        if (localStorage.getItem('token')) {
            navigate('/', { replace: true });
            return;
        }
        const params = new URLSearchParams(location.search);
        const errorMsg = params.get('error');
        if (errorMsg) {
            toast.error(errorMsg);
            navigate(location.pathname, { replace: true }); // URL'den ?error kaldır
        }
    }, [navigate, location.search, location.pathname]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        try {
            await login(email);
            toast.success('Doğrulama kodu yollandı!');
            setTimeout(() => {
                navigate('/verify', { state: { email, from: returnTo } });
            }, 1000);
        } catch (error) {
            console.error('Login error:', error);
            if (error.response?.data?.message) {
                toast.error(error.response.data.message);
            } else {
                toast.error('Giriş yapılamadı. Tekrar deneyin.');
            }
            setIsLoading(false);
        }
    };

    const debouncedSubmit = useDebouncedCallback(handleSubmit, 500);

    return (
        <div className="login-page-wrap">
            <button type="button" className="login-back-btn" onClick={() => navigate('/welcome')}>
                <FaArrowLeft />
                <span>Geri Dön</span>
            </button>

            <div className="login-card">
                <div className="login-card-header">
                    <img src="/logo.png" alt="Dear Future" className="login-card-logo" />
                    <h1>Hoş Geldiniz</h1>
                    <p className="login-card-tagline">Zamanın ötesine bir not bırakmaya hazır mısın?</p>
                    <p className="login-card-subtitle">Yolculuğuna başlamak için e-postanı gir.</p>
                </div>

                <div className="login-card-body">
                    <form onSubmit={debouncedSubmit} className="login-form">
                        <div className={`login-input-wrap ${isFocused || email ? 'active' : ''}`}>
                            <label htmlFor="login-email">E-POSTA ADRESI</label>
                            <div className="login-input-inner">
                                <FaEnvelope className="login-input-icon" />
                                <input
                                    id="login-email"
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    onFocus={() => setIsFocused(true)}
                                    onBlur={() => setIsFocused(false)}
                                    placeholder=""
                                    required
                                    disabled={isLoading}
                                />
                            </div>
                        </div>

                        <button type="submit" className="login-submit-btn" disabled={isLoading}>
                            {isLoading ? (
                                <div className="login-btn-loader" />
                            ) : (
                                <>
                                    <span>Sihirli Bağlantı Gönder</span>
                                    <FaPaperPlane />
                                </>
                            )}
                        </button>

                        <div className="login-divider">
                            <span>veya</span>
                        </div>

                        <button
                            type="button"
                            className="login-google-btn"
                            onClick={() => {
                                // OAuth session backend'de (api.dearfuture.info) tutulur; link mutlaka backend domain'ine gitmeli.
                                // dearfuture.com.tr üzerinden giderse cookie farklı domain'de kalır → authorization_request_not_found.
                                const envUrl = (import.meta.env.VITE_BACKEND_URL || '').replace(/\/$/, '');
                                const isFrontendOrigin = typeof window !== 'undefined' && /dearfuture\.com\.tr$/i.test(window.location.origin);
                                const base = (isFrontendOrigin || !envUrl) ? 'https://api.dearfuture.info' : envUrl;
                                window.location.href = `${base}/oauth2/authorization/google`;
                            }}
                        >
                            <FaGoogle className="login-google-icon" />
                            <span>Google ile Giriş Yap</span>
                        </button>
                    </form>

                    <p className="login-agreement">
                        Giriş yaparsanız{' '}
                        <Link to="/terms" className="login-agreement-link">Kullanım Şartları</Link>,{' '}
                        <Link to="/privacy" className="login-agreement-link">Gizlilik Politikası</Link>
                        {' '}ve{' '}
                        <Link to="/cookie-policy" className="login-agreement-link">Çerez Politikası</Link>
                        {' '}sözleşmelerini onaylamış kabul edilirsiniz.
                    </p>

                    <div className="login-card-footer">
                        <div className="login-secure-badge">
                            <FaLock />
                            <span>Uçtan Uca Şifreli & Güvenli</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
