import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { FaBars, FaTimes, FaSun, FaMoon } from 'react-icons/fa';
import { getTheme, toggleTheme } from '../theme';
import './Header.css';

const Header = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [dark, setDark] = useState(() => getTheme() === 'dark');
    const navigate = useNavigate();
    const location = useLocation();
    const token = localStorage.getItem('token');

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    const handleThemeToggle = () => {
        const next = toggleTheme();
        setDark(next === 'dark');
    };

    const toggleMenu = () => setIsOpen(!isOpen);
    const closeMenu = () => setIsOpen(false);

    return (
        <header className="header">
            <div className="header-container">
                <Link to="/" className="logo" onClick={closeMenu}>
                    <img src="/logo.png" alt="Dear Future" className="logo-img" />
                    <span>Dear Future</span>
                </Link>

                <div className="mobile-menu-icon" onClick={toggleMenu}>
                    {isOpen ? <FaTimes /> : <FaBars />}
                </div>

                <nav className={`nav-menu ${isOpen ? 'active' : ''}`}>
                    {token ? (
                        <>
                            <Link
                                to="/"
                                className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}
                                onClick={closeMenu}
                            >
                                Panel
                            </Link>
                            <Link
                                to="/new"
                                className={`nav-link ${location.pathname === '/new' ? 'active' : ''}`}
                                onClick={closeMenu}
                            >
                                Yeni Mesaj
                            </Link>
                            <Link
                                to="/change-subscription"
                                className={`nav-link ${location.pathname === '/change-subscription' ? 'active' : ''}`}
                                onClick={closeMenu}
                            >
                                Abonelik
                            </Link>
                            <Link
                                to="/settings"
                                className={`nav-link ${location.pathname === '/settings' ? 'active' : ''}`}
                                onClick={closeMenu}
                            >
                                Ayarlar
                            </Link>
                            <Link
                                to="/profile"
                                className={`nav-link ${location.pathname === '/profile' ? 'active' : ''}`}
                                onClick={closeMenu}
                            >
                                Profil
                            </Link>
                            <button type="button" className="theme-toggle" onClick={handleThemeToggle} title={dark ? 'Açık tema' : 'Koyu tema'} aria-label="Tema değiştir">
                                {dark ? <FaSun /> : <FaMoon />}
                            </button>
                            <button onClick={handleLogout} className="logout-btn">
                                Çıkış Yap
                            </button>
                        </>
                    ) : (
                        <>
                            <Link
                                to="/welcome"
                                className={`nav-link ${location.pathname === '/welcome' ? 'active' : ''}`}
                                onClick={closeMenu}
                            >
                                Ana Sayfa
                            </Link>
                            <Link
                                to="/pricing"
                                className={`nav-link ${location.pathname === '/pricing' ? 'active' : ''}`}
                                onClick={closeMenu}
                            >
                                Fiyatlandırma
                            </Link>
                            <Link
                                to="/login"
                                className="login-link"
                                onClick={closeMenu}
                            >
                                Giriş Yap
                            </Link>
                        </>
                    )}
                </nav>
            </div>
        </header>
    );
};

export default Header;
