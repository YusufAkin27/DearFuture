import { useState } from 'react';
import { toast } from 'react-toastify';
import { FaEnvelope, FaMapMarkerAlt, FaPhone } from 'react-icons/fa';
import { sendContactMessage, verifyContactEmail } from '../api/contact';
import { PinInput } from '../components/base/pin-input';
import './ContactPage.css';

const NAME_MIN = 2;
const NAME_MAX = 200;
const SUBJECT_MIN = 3;
const SUBJECT_MAX = 500;
const MESSAGE_MIN = 10;
const MESSAGE_MAX = 5000;
const PHONE_MAX = 20;

const ContactPage = () => {
    const [step, setStep] = useState('form');
    const [loading, setLoading] = useState(false);
    const [verifyCode, setVerifyCode] = useState('');
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        subject: '',
        message: '',
        phone: '',
    });
    const [errors, setErrors] = useState({});

    const validateForm = () => {
        const e = {};
        const name = (formData.name || '').trim();
        const email = (formData.email || '').trim();
        const subject = (formData.subject || '').trim();
        const message = (formData.message || '').trim();
        const phone = (formData.phone || '').trim();

        if (name.length < NAME_MIN || name.length > NAME_MAX) {
            e.name = `Ad Soyad ${NAME_MIN}-${NAME_MAX} karakter olmalıdır.`;
        }
        if (!email) {
            e.email = 'E-posta adresi zorunludur.';
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            e.email = 'Geçerli bir e-posta adresi giriniz.';
        }
        if (subject.length < SUBJECT_MIN || subject.length > SUBJECT_MAX) {
            e.subject = `Konu ${SUBJECT_MIN}-${SUBJECT_MAX} karakter olmalıdır.`;
        }
        if (message.length < MESSAGE_MIN || message.length > MESSAGE_MAX) {
            e.message = `Mesaj ${MESSAGE_MIN}-${MESSAGE_MAX} karakter olmalıdır.`;
        }
        if (phone.length > PHONE_MAX) {
            e.phone = `Telefon en fazla ${PHONE_MAX} karakter olabilir.`;
        }
        setErrors(e);
        return Object.keys(e).length === 0;
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors((prev) => ({ ...prev, [name]: null }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;
        setLoading(true);
        setErrors({});
        try {
            const res = await sendContactMessage({
                name: formData.name.trim(),
                email: formData.email.trim(),
                subject: formData.subject.trim(),
                message: formData.message.trim(),
                phone: formData.phone.trim() || undefined,
            });
            const data = res.data;
            if (data && data.success) {
                toast.success(data.message || 'Mesajınız alındı. E-postanıza gönderilen kodu girin.');
                setStep('verify');
                setVerifyCode('');
            } else {
                toast.error(data?.message || 'Mesaj gönderilemedi.');
            }
        } catch (err) {
            const msg = err.response?.data?.message || 'Mesaj gönderilemedi. Lütfen tekrar deneyin.';
            toast.error(msg);
        } finally {
            setLoading(false);
        }
    };

    const handleVerify = async (e) => {
        e.preventDefault();
        const code = (verifyCode || '').replace(/\D/g, '').replace(/\s/g, '');
        if (code.length !== 6 || !/^\d{6}$/.test(code)) {
            toast.error('Doğrulama kodu 6 haneli sayı olmalıdır.');
            return;
        }
        setLoading(true);
        try {
            const res = await verifyContactEmail(code);
            const data = res.data;
            if (data && data.success) {
                toast.success(data.message || 'E-postanız doğrulandı. Teşekkür ederiz.');
                setStep('done');
            } else {
                toast.error(data?.message || 'Doğrulama başarısız.');
            }
        } catch (err) {
            const msg = err.response?.data?.message || 'Doğrulama başarısız. Lütfen kodu kontrol edin.';
            toast.error(msg);
        } finally {
            setLoading(false);
        }
    };

    if (step === 'done') {
        return (
            <section className="contact-container">
                <div className="contact-inner">
                    <div className="contact-done">
                        <span className="contact-pill">Teşekkürler</span>
                        <h1>Mesajınız alındı</h1>
                        <p>E-postanız doğrulandı. En kısa sürede size dönüş yapacağız.</p>
                    </div>
                </div>
            </section>
        );
    }

    if (step === 'verify') {
        return (
            <section className="contact-container">
                <div className="contact-inner">
                    <header className="contact-hero">
                        <span className="contact-pill">Doğrulama</span>
                        <h1>E-posta doğrulama</h1>
                        <p>
                            <strong>{formData.email}</strong> adresine gönderilen 6 haneli kodu girin. (Kod 15 dakika geçerlidir)
                        </p>
                    </header>
                    <div className="contact-verify-wrap">
                        <form className="contact-form contact-verify-form" onSubmit={handleVerify}>
                            <div className="contact-form-group">
                                <PinInput value={verifyCode} onChange={setVerifyCode} size="md">
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
                                    <PinInput.Description>E-postanıza gelen 6 haneli kodu girin. (Kod 15 dakika geçerlidir)</PinInput.Description>
                                </PinInput>
                            </div>
                            <button type="submit" className="contact-submit-btn" disabled={loading}>
                                {loading ? 'Doğrulanıyor...' : 'Doğrula'}
                            </button>
                        </form>
                    </div>
                </div>
            </section>
        );
    }

    return (
        <section className="contact-container">
            <div className="contact-inner">
                <header className="contact-hero">
                    <span className="contact-pill">İletişim</span>
                    <h1>Bizimle iletişime geçin</h1>
                    <p>
                        Sorularınız veya önerileriniz için formu doldurun. E-postanıza gönderilen doğrulama kodu ile mesajınızı onaylayın.
                    </p>
                </header>

                <div className="contact-grid">
                    <div className="contact-info">
                        <div className="contact-info-item">
                            <FaEnvelope className="contact-info-icon" />
                            <div>
                                <h3>E-posta</h3>
                                <p>merhaba@dearfuture.com</p>
                                <p>destek@dearfuture.com</p>
                            </div>
                        </div>
                        <div className="contact-info-item">
                            <FaMapMarkerAlt className="contact-info-icon" />
                            <div>
                                <h3>Adres</h3>
                                <p>Örnek Mah. Gelecek Sok. No: 1</p>
                                <p>34000 İstanbul</p>
                            </div>
                        </div>
                        <div className="contact-info-item">
                            <FaPhone className="contact-info-icon" />
                            <div>
                                <h3>Telefon</h3>
                                <p>+90 (212) 000 00 00</p>
                            </div>
                        </div>
                    </div>

                    <form className="contact-form" onSubmit={handleSubmit}>
                        <div className="contact-form-group">
                            <label htmlFor="contact-name">Ad Soyad *</label>
                            <input
                                type="text"
                                id="contact-name"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                placeholder="Adınız Soyadınız"
                                maxLength={NAME_MAX}
                                className={errors.name ? 'contact-input-error' : ''}
                            />
                            {errors.name && <span className="contact-field-error">{errors.name}</span>}
                        </div>
                        <div className="contact-form-group">
                            <label htmlFor="contact-email">E-posta *</label>
                            <input
                                type="email"
                                id="contact-email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                placeholder="ornek@email.com"
                                className={errors.email ? 'contact-input-error' : ''}
                            />
                            {errors.email && <span className="contact-field-error">{errors.email}</span>}
                        </div>
                        <div className="contact-form-group">
                            <label htmlFor="contact-subject">Konu *</label>
                            <input
                                type="text"
                                id="contact-subject"
                                name="subject"
                                value={formData.subject}
                                onChange={handleChange}
                                placeholder="Mesajınızın konusu"
                                maxLength={SUBJECT_MAX}
                                className={errors.subject ? 'contact-input-error' : ''}
                            />
                            {errors.subject && <span className="contact-field-error">{errors.subject}</span>}
                        </div>
                        <div className="contact-form-group">
                            <label htmlFor="contact-message">Mesajınız *</label>
                            <textarea
                                id="contact-message"
                                name="message"
                                value={formData.message}
                                onChange={handleChange}
                                rows={5}
                                placeholder="En az 10 karakter..."
                                maxLength={MESSAGE_MAX}
                                className={errors.message ? 'contact-input-error' : ''}
                            />
                            {errors.message && <span className="contact-field-error">{errors.message}</span>}
                            <span className="contact-char-count">{formData.message.length} / {MESSAGE_MAX}</span>
                        </div>
                        <div className="contact-form-group">
                            <label htmlFor="contact-phone">Telefon (isteğe bağlı)</label>
                            <input
                                type="tel"
                                id="contact-phone"
                                name="phone"
                                value={formData.phone}
                                onChange={handleChange}
                                placeholder="+90 5XX XXX XX XX"
                                maxLength={PHONE_MAX}
                                className={errors.phone ? 'contact-input-error' : ''}
                            />
                            {errors.phone && <span className="contact-field-error">{errors.phone}</span>}
                        </div>
                        <button type="submit" className="contact-submit-btn" disabled={loading}>
                            {loading ? 'Gönderiliyor...' : 'Mesaj Gönder'}
                        </button>
                    </form>
                </div>
            </div>
        </section>
    );
};

export default ContactPage;
