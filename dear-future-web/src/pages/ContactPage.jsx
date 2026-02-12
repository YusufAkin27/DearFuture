import { useState } from 'react';
import './ContactPage.css';
import { FaEnvelope, FaMapMarkerAlt, FaPhone } from 'react-icons/fa';

const ContactPage = () => {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        message: '',
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        alert('Mesajınız alındı. (Demo)');
    };

    return (
        <section className="contact-container">
            <div className="contact-inner">
                <header className="contact-hero">
                    <span className="contact-pill">İletişim</span>
                    <h1>Bizimle iletişime geçin</h1>
                    <p>
                        Sorularınız veya önerileriniz için aşağıdaki bilgilerden bize ulaşabilirsiniz.
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
                            <label htmlFor="contact-name">Ad Soyad</label>
                            <input
                                type="text"
                                id="contact-name"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="contact-form-group">
                            <label htmlFor="contact-email">E-posta</label>
                            <input
                                type="email"
                                id="contact-email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="contact-form-group">
                            <label htmlFor="contact-message">Mesajınız</label>
                            <textarea
                                id="contact-message"
                                name="message"
                                value={formData.message}
                                onChange={handleChange}
                                rows={5}
                                required
                            />
                        </div>
                        <button type="submit" className="contact-submit-btn">
                            Mesaj Gönder
                        </button>
                    </form>
                </div>
            </div>
        </section>
    );
};

export default ContactPage;
