import { Link } from 'react-router-dom';
import { FaInstagram, FaLinkedin } from 'react-icons/fa';
import './Footer.css';

const Footer = () => {
    return (
        <footer className="footer" role="contentinfo">
            <div className="footer-bar">
                <div className="footer-bar-inner">
                    <div className="footer-brand-block">
                        <Link to="/welcome" className="footer-brand">
                            <img src="/logo.png" alt="Dear Future" className="footer-logo-img" />
                            <span>Dear Future</span>
                        </Link>
                        <p className="footer-tagline">
                            Geleceğe bugünden iz bırakın.
                        </p>
                        <div className="footer-social">
                            <a href="#" className="footer-social-link" aria-label="Instagram"><FaInstagram /></a>
                            <a href="#" className="footer-social-link" aria-label="LinkedIn"><FaLinkedin /></a>
                        </div>
                    </div>

                    <div className="footer-links-row">
                        <div className="footer-col">
                            <span className="footer-col-label">Keşfet</span>
                            <Link to="/public-messages" className="footer-link">Herkese Açık Mesajlar</Link>
                        </div>
                        <div className="footer-col">
                            <span className="footer-col-label">Kurumsal</span>
                            <Link to="/about" className="footer-link">Hakkımızda</Link>
                            <Link to="/contact" className="footer-link">İletişim</Link>
                        </div>
                        <div className="footer-col">
                            <span className="footer-col-label">Yasal</span>
                            <Link to="/privacy" className="footer-link">Gizlilik</Link>
                            <Link to="/terms" className="footer-link">Kullanım Şartları</Link>
                            <Link to="/cookie-policy" className="footer-link">Çerez</Link>
                            <Link to="/security" className="footer-link">Güvenlik</Link>
                        </div>
                    </div>
                </div>

                <div className="footer-bottom">
                    <span>&copy; {new Date().getFullYear()} Dear Future. Tüm hakları saklıdır.</span>
                </div>
            </div>
        </footer>
    );
};

export default Footer;
