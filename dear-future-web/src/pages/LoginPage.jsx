import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
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
        }
    }, [navigate]);

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
                                // OAuth2 session backend'de tutulduğu için tarayıcı doğrudan backend'e gitmeli (proxy ile cookie/8080 uyuşmaz).
                                const base = import.meta.env.VITE_BACKEND_URL
                                    || (import.meta.env.DEV ? 'http://localhost:8080' : '');
                                const url = base ? `${base.replace(/\/$/, '')}/oauth2/authorization/google` : '/oauth2/authorization/google';
                                window.location.href = url;
                            }}
                        >
                            <FaGoogle className="login-google-icon" />
                            <span>Google ile Giriş Yap</span>
                        </button>
                    </form>

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
