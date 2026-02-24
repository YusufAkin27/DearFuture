import { useState } from 'react';
import { sendCode, verifyCode } from '../api/auth';
import './LoginPage.css';

export default function LoginPage({ onSuccess }) {
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [step, setStep] = useState('email');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSendCode = async (e) => {
    e.preventDefault();
    setError('');
    if (!email.trim()) return setError('E-posta girin.');
    setLoading(true);
    try {
      await sendCode(email.trim());
      setStep('code');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Kod gönderilemedi.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async (e) => {
    e.preventDefault();
    setError('');
    if (!code.trim()) return setError('Doğrulama kodunu girin.');
    setLoading(true);
    try {
      const { data } = await verifyCode(email.trim(), code.trim());
      if (data?.token) {
        localStorage.setItem('adminToken', data.token);
        onSuccess();
      } else {
        setError('Giriş başarısız.');
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message;
      setError(msg || 'Kod geçersiz veya süresi dolmuş.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>Dear Future Admin</h1>
        <p className="login-sub">E-posta ile giriş yapın (admin hesabı)</p>
        {error && <div className="login-error">{error}</div>}
        {step === 'email' ? (
          <form onSubmit={handleSendCode}>
            <input
              type="email"
              placeholder="E-posta"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="login-input"
              autoFocus
              disabled={loading}
            />
            <button type="submit" className="login-btn" disabled={loading}>
              {loading ? 'Gönderiliyor…' : 'Giriş kodu gönder'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleVerify}>
            <input
              type="text"
              placeholder="Doğrulama kodu"
              value={code}
              onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
              className="login-input"
              maxLength={6}
              autoFocus
              disabled={loading}
            />
            <button type="submit" className="login-btn" disabled={loading}>
              {loading ? 'Doğrulanıyor…' : 'Giriş yap'}
            </button>
            <button
              type="button"
              className="login-back"
              onClick={() => { setStep('email'); setCode(''); setError(''); }}
              disabled={loading}
            >
              Farklı e-posta
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
