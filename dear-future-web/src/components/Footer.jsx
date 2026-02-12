import { Link } from 'react-router-dom';
import { FaInstagram, FaLinkedin } from 'react-icons/fa';
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
                            <a href="#" className="social-link" aria-label="Instagram"><FaInstagram /></a>
                            <a href="#" className="social-link" aria-label="LinkedIn"><FaLinkedin /></a>
                        </div>
                    </div>

                    <div className="footer-links-grid">
                        <div className="link-column">
                            <h3>Kurumsal</h3>
                            <ul>
                                <li><Link to="/about">Hakkımızda</Link></li>
                                <li><Link to="/contact">İletişim</Link></li>
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

                  
                </div>

                <div className="footer-bottom">
                    <p>&copy; {new Date().getFullYear()} DearFuture. Tüm hakları saklıdır.</p>
              
                </div>
            </div>
        </footer>
    );
};

export default Footer;
