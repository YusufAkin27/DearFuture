import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { FaBars, FaTimes } from 'react-icons/fa';
import './Navbar.css';

const Navbar = () => {
    const [isOpen, setIsOpen] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();
    const token = localStorage.getItem('token');

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    const toggleMenu = () => setIsOpen(!isOpen);

    // Close menu when route changes
    const closeMenu = () => setIsOpen(false);

    return (
        <nav className="navbar">
            <div className="navbar-container">
                <Link to="/" className="logo" onClick={closeMenu}>
                    DearFuture
                </Link>

                <div className="mobile-menu-icon" onClick={toggleMenu}>
                    {isOpen ? <FaTimes /> : <FaBars />}
                </div>

                <div className={`nav-menu ${isOpen ? 'active' : ''}`}>
                    {token ? (
                        <>
                            <Link
                                to="/"
                                className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}
                                onClick={closeMenu}
                            >
                                Dashboard
                            </Link>
                            <Link
                                to="/new"
                                className={`nav-link ${location.pathname === '/new' ? 'active' : ''}`}
                                onClick={closeMenu}
                            >
                                New Message
                            </Link>
                            <Link
                                to="/profile"
                                className={`nav-link ${location.pathname === '/profile' ? 'active' : ''}`}
                                onClick={closeMenu}
                            >
                                Profile
                            </Link>
                            <button onClick={handleLogout} className="logout-btn">
                                Logout
                            </button>
                        </>
                    ) : (
                        <Link
                            to="/login"
                            className="nav-link"
                            onClick={closeMenu}
                        >
                            Login
                        </Link>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
