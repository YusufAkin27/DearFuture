import { Link } from 'react-router-dom';
import { FaHeart, FaTwitter, FaInstagram, FaLinkedin, FaGithub, FaPaperPlane } from 'react-icons/fa';
import './Footer.css';

const Footer = () => {
    return (
        <footer className="footer">
            <div className="footer-content">
                <div className="footer-top">
                    <div className="footer-logo-section">
                        <Link to="/" className="footer-brand">
                            <img src="/logo.png" alt="Dear Future" className="footer-logo-img" />
                            <span>Dear Future</span>
                        </Link>
                        <p className="footer-description">
                            Geleceğe bugünden iz bırakın. Sevdiklerinize asla kaybolmayacak dijital mektuplar ve anılar gönderin.
                        </p>
                        <div className="social-links">
                            <a href="#" className="social-link"><FaTwitter /></a>
                            <a href="#" className="social-link"><FaInstagram /></a>
                            <a href="#" className="social-link"><FaLinkedin /></a>
                            <a href="#" className="social-link"><FaGithub /></a>
                        </div>
                    </div>

                    <div className="footer-links-grid">
                        <div className="link-column">
                            <h3>Platform</h3>
                            <ul>
                                <li><Link to="/">Ana Sayfa</Link></li>
                                <li><Link to="/pricing">Fiyatlandırma</Link></li>
                                <li><Link to="/features">Özellikler</Link></li>
                                <li><Link to="/blog">Blog</Link></li>
                            </ul>
                        </div>
                        <div className="link-column">
                            <h3>Kurumsal</h3>
                            <ul>
                                <li><Link to="/about">Hakkımızda</Link></li>
                                <li><Link to="/contact">İletişim</Link></li>
                                <li><Link to="/careers">Kariyer</Link></li>
                                <li><Link to="/press">Basın</Link></li>
                            </ul>
                        </div>
                        <div className="link-column">
                            <h3>Yasal</h3>
                            <ul>
                                <li><Link to="/privacy">Gizlilik Politikası</Link></li>
                                <li><Link to="/terms">Kullanım Şartları</Link></li>
                                <li><Link to="/security">Güvenlik</Link></li>
                            </ul>
                        </div>
                    </div>

                    <div className="footer-newsletter">
                        <h3>Bültenimize Abone Olun</h3>
                        <p>Yeniliklerden haberdar olmak için e-posta listemize katılın.</p>
                        <form className="newsletter-form" onSubmit={(e) => e.preventDefault()}>
                            <div className="input-group">
                                <input type="email" placeholder="E-posta adresiniz" />
                                <button type="submit" aria-label="Abone Ol">
                                    <FaPaperPlane />
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <div className="footer-bottom">
                    <p>&copy; {new Date().getFullYear()} DearFuture. Tüm hakları saklıdır.</p>
              
                </div>
            </div>
        </footer>
    );
};

export default Footer;
