import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { invalidatePrefix } from '../api/cache';
import './AuthCallbackPage.css';

/**
 * Google OAuth2 giriş sonrası backend bu sayfaya yönlendirir: ?token=... veya ?error=...
 * Token varsa kaydedip ana sayfaya, hata varsa login sayfasına yönlendirir.
 */
const AuthCallbackPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const error = searchParams.get('error');

    useEffect(() => {
        if (token) {
            localStorage.setItem('token', token);
            invalidatePrefix('get:/user');
            toast.success('Google ile giriş başarılı! Hoş geldiniz.');
            navigate('/', { replace: true });
            return;
        }
        if (error) {
            toast.error(error || 'Google ile giriş yapılamadı.');
            navigate('/login', { replace: true });
            return;
        }
        navigate('/login', { replace: true });
    }, [token, error, navigate]);

    return (
        <div className="auth-callback-wrap">
            <div className="auth-callback-spinner" />
            <p>Giriş tamamlanıyor...</p>
        </div>
    );
};

export default AuthCallbackPage;
