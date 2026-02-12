import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { FaBars, FaTimes, FaSun, FaMoon } from 'react-icons/fa';
import { getTheme, toggleTheme } from '../theme';
import './Header.css';

const Header = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [dark, setDark] = useState(() => getTheme() === 'dark');
    const [scrolled, setScrolled] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();
    const token = localStorage.getItem('token');

    useEffect(() => {
        const onScroll = () => setScrolled(window.scrollY > 8);
        window.addEventListener('scroll', onScroll, { passive: true });
        return () => window.removeEventListener('scroll', onScroll);
    }, []);

    useEffect(() => {
        if (isOpen) document.body.classList.add('header-menu-open');
        else document.body.classList.remove('header-menu-open');
        return () => document.body.classList.remove('header-menu-open');
    }, [isOpen]);

    const handleLogout = () => {
        setIsOpen(false);
        localStorage.removeItem('token');
        navigate('/login');
    };

    const handleThemeToggle = () => {
        const next = toggleTheme();
        setDark(next === 'dark');
    };

    const toggleMenu = () => setIsOpen((prev) => !prev);
    const closeMenu = () => setIsOpen(false);

    return (
        <header className={`header ${scrolled ? 'header--scrolled' : ''}`} role="banner">
            <div className="header__backdrop" aria-hidden="true" onClick={closeMenu} data-open={isOpen} />
            <div className="header__container">
                <Link to="/" className="header__logo" onClick={closeMenu} aria-label="Dear Future Ana Sayfa">
                    <img src="/logo.png" alt="" className="header__logo-img" />
                    <span className="header__logo-text">Dear Future</span>
                </Link>

                <button
                    type="button"
                    className="header__burger"
                    onClick={toggleMenu}
                    aria-expanded={isOpen}
                    aria-controls="header-nav"
                    aria-label={isOpen ? 'Menüyü kapat' : 'Menüyü aç'}
                >
                    <span className="header__burger-bar" data-open={isOpen} />
                    <span className="header__burger-bar" data-open={isOpen} />
                    <span className="header__burger-bar" data-open={isOpen} />
                </button>

                <nav id="header-nav" className={`header__nav ${isOpen ? 'header__nav--open' : ''}`} aria-label="Ana navigasyon">
                    <div className="header__nav-inner">
                        {token ? (
                            <>
                                <div className="header__nav-links">
                                    <Link to="/new" className={`header__link ${location.pathname === '/new' ? 'header__link--active' : ''}`} onClick={closeMenu}>Yeni Mesaj</Link>
                                    <Link to="/change-subscription" className={`header__link ${location.pathname === '/change-subscription' ? 'header__link--active' : ''}`} onClick={closeMenu}>Abonelik</Link>
                                    <Link to="/settings" className={`header__link ${location.pathname === '/settings' ? 'header__link--active' : ''}`} onClick={closeMenu}>Ayarlar</Link>
                                    <Link to="/profile" className={`header__link ${location.pathname === '/profile' ? 'header__link--active' : ''}`} onClick={closeMenu}>Profil</Link>
                                </div>
                                <div className="header__actions">
                                    <button type="button" className="header__theme" onClick={handleThemeToggle} title={dark ? 'Açık tema' : 'Koyu tema'} aria-label="Tema değiştir">
                                        {dark ? <FaSun /> : <FaMoon />}
                                    </button>
                                    <button type="button" onClick={handleLogout} className="header__logout">Çıkış Yap</button>
                                </div>
                            </>
                        ) : (
                            <>
                                <div className="header__nav-links">
                                    <Link to="/welcome" className={`header__link ${location.pathname === '/welcome' ? 'header__link--active' : ''}`} onClick={closeMenu}>Ana Sayfa</Link>
                                    <Link to="/features" className={`header__link ${location.pathname === '/features' ? 'header__link--active' : ''}`} onClick={closeMenu}>Özellikler</Link>
                                    <Link to="/blog" className={`header__link ${location.pathname === '/blog' ? 'header__link--active' : ''}`} onClick={closeMenu}>Blog</Link>
                                    <Link to="/pricing" className={`header__link ${location.pathname === '/pricing' ? 'header__link--active' : ''}`} onClick={closeMenu}>Fiyatlandırma</Link>
                                </div>
                                <Link to="/login" className="header__cta" onClick={closeMenu}>Giriş Yap</Link>
                            </>
                        )}
                    </div>
                </nav>
            </div>
        </header>
    );
};

export default Header;
