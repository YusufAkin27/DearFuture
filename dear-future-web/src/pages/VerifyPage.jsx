import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaRedo, FaArrowLeft } from 'react-icons/fa';
import { verifyCode, resendCode } from '../api/auth';
import { PinInput } from '../components/base/pin-input';
import './VerifyPage.css';

const VerifyPage = () => {
    const [code, setCode] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [isResending, setIsResending] = useState(false);
    const location = useLocation();
    const navigate = useNavigate();
    const email = location.state?.email;
    const [timer, setTimer] = useState(60);

    useEffect(() => {
        if (localStorage.getItem('token')) {
            navigate('/', { replace: true });
            return;
        }
        if (!email) {
            navigate('/login', { replace: true });
            return;
        }
    }, [email, navigate]);

    useEffect(() => {
        if (!email) return;
        const interval = setInterval(() => setTimer((prev) => (prev > 0 ? prev - 1 : 0)), 1000);
        return () => clearInterval(interval);
    }, [email]);

    const handleVerify = async (e) => {
        e.preventDefault();
        const digitsOnly = (code || '').replace(/\D/g, '').replace(/\s/g, '');
        if (digitsOnly.length !== 6) {
            toast.error('Lütfen 6 haneli kodu girin.');
            return;
        }
        setIsLoading(true);
        try {
            const response = await verifyCode(email, digitsOnly);
            const token = response.data.token;

            localStorage.setItem('token', token);
            toast.success('Giriş başarılı! Hoş geldiniz.');
            const from = location.state?.from || '/';
            navigate(from, { replace: true });
        } catch (error) {
            console.error(error);
            if (error.response?.data?.message) {
                toast.error(error.response.data.message);
            } else {
                toast.error('Doğrulama başarısız. Kodu kontrol edin.');
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleResend = async () => {
        if (timer > 0) return;

        setIsResending(true);
        try {
            await resendCode(email);
            toast.success('Yeni kod gönderildi!');
            setTimer(60); // Reset timer
        } catch (error) {
            console.error(error);
            toast.error('Kod gönderilemedi. Lütfen bekleyin.');
        } finally {
            setIsResending(false);
        }
    };

    const handleLoginRedirect = () => {
        navigate('/login');
    };

    return (
        <div className="modern-verify-container">
            <div className="aurora-bg">
                <div className="aurora-blob blob-1"></div>
                <div className="aurora-blob blob-2"></div>
                <div className="aurora-blob blob-3"></div>
            </div>

            <button className="back-nav-btn" onClick={handleLoginRedirect}>
                <FaArrowLeft />
                <span>Giriş'e Dön</span>
            </button>

            <div className="modern-verify-card">
                <div className="card-header">
                    <h1>Doğrulama</h1>
                    <p className="verify-instruction">E-posta adresinize gönderilen 6 haneli kodu girin.</p>
                </div>

                <form onSubmit={handleVerify} className="modern-form">
                    <div className="code-input-wrapper">
                        <PinInput value={code} onChange={setCode} size="md">
                            <PinInput.Label>Doğrulama kodu</PinInput.Label>
                            <PinInput.Group maxLength={6}>
                                <PinInput.Slot index={0} />
                                <PinInput.Slot index={1} />
                                <PinInput.Slot index={2} />
                                <PinInput.Separator />
                                <PinInput.Slot index={3} />
                                <PinInput.Slot index={4} />
                                <PinInput.Slot index={5} />
                            </PinInput.Group>
                            <PinInput.Description>E-posta adresinize gönderilen 6 haneli kodu girin.</PinInput.Description>
                        </PinInput>
                    </div>
                </form>

                <div className="resend-section">
                    <button
                        className={`resend-button ${timer > 0 ? 'disabled' : ''}`}
                        onClick={handleResend}
                        disabled={timer > 0 || isResending}
                    >
                        {isResending ? (
                            <div className="spinner-small"></div>
                        ) : (
                            <FaRedo className={isResending ? 'spinning' : ''} />
                        )}
                        <span>
                            {timer > 0
                                ? `Tekrar gönder (${timer}s)`
                                : 'Kodu Tekrar Gönder'}
                        </span>
                    </button>
                </div>
            </div>
        </div>
    );
};

export default VerifyPage;
