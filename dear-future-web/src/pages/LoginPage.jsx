import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaEnvelope, FaPaperPlane, FaLock, FaArrowLeft } from 'react-icons/fa';
import { login } from '../api/auth';
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
                navigate(`/verify?email=${encodeURIComponent(email)}`, { state: { from: returnTo } });
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

    return (
        <div className="modern-login-container">
            <div className="aurora-bg">
                <div className="aurora-blob blob-1"></div>
                <div className="aurora-blob blob-2"></div>
                <div className="aurora-blob blob-3"></div>
            </div>

            <button className="back-nav-btn" onClick={() => navigate('/welcome')}>
                <FaArrowLeft />
                <span>Geri Dön</span>
            </button>

            <div className="modern-login-card">
                <div className="card-header">
                    <img src="/logo.png" alt="Dear Future" className="card-header-logo" />
                    <h1>Hoş Geldiniz</h1>
                    <p>Yolculuğunuza devam etmek için giriş yapın.</p>
                </div>

                <form onSubmit={handleSubmit} className="modern-form">
                    <div className={`modern-input-group ${isFocused || email ? 'active' : ''}`}>
                        <label htmlFor="email">E-posta Adresi</label>
                        <div className="input-field-wrapper">
                            <FaEnvelope className="field-icon" />
                            <input
                                id="email"
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                onFocus={() => setIsFocused(true)}
                                onBlur={() => setIsFocused(false)}
                                required
                                disabled={isLoading}
                            />
                        </div>
                    </div>

                    <button type="submit" className="modern-submit-btn" disabled={isLoading}>
                        {isLoading ? (
                            <div className="btn-loader"></div>
                        ) : (
                            <>
                                <span>Sihirli Bağlantı Gönder</span>
                                <FaPaperPlane />
                            </>
                        )}
                    </button>
                </form>

                <div className="card-footer">
                    <div className="secure-badge">
                        <FaLock />
                        <span>Uçtan Uca Şifreli & Güvenli</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
