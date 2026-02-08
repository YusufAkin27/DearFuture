import { useNavigate } from 'react-router-dom';
import { FaArrowRight, FaClock, FaLock, FaMagic, FaThLarge } from 'react-icons/fa';
import './WelcomePage.css';

const WelcomePage = () => {
    const navigate = useNavigate();
    const isLoggedIn = !!localStorage.getItem('token');

    const handleStart = () => {
        navigate('/login');
    };

    const handleDashboard = () => {
        navigate('/');
    };

    return (
        <div className="welcome-container">
            <div className="hero-content">
                <div className="welcome-logo-block">
                    <img src="/logo.png" alt="Dear Future" className="welcome-logo-img" />
                    <p className="welcome-brand">Dear Future</p>
                </div>
             

                <h1 className="welcome-title">
                    Bugünden Geleceğe,<br />
                    <span className="gradient-text">Bir Mesajın Var</span>
                </h1>

                <p className="welcome-message">
                    Kendinize, sevdiklerinize veya çocuklarınıza yıllar sonra ulaşacak
                    dijital mektuplar, fotoğraflar ve videolar saklayın.
                </p>

                {isLoggedIn ? (
                    <button className="cta-button" onClick={handleDashboard}>
                        <span>Panele Git</span>
                        <FaThLarge className="button-icon" />
                    </button>
                ) : (
                    <button className="cta-button" onClick={handleStart}>
                        <span>Hemen Başla</span>
                        <FaArrowRight className="button-icon" />
                    </button>
                )}

                <div className="features-grid">
                    <div className="feature-item">
                        <div className="feature-icon-wrapper">
                            <FaClock />
                        </div>
                        <h3>Zaman Ayarlı</h3>
                        <p>Mesajınızın iletileceği tarihi tam olarak siz belirleyin.</p>
                    </div>
                    <div className="feature-item">
                        <div className="feature-icon-wrapper">
                            <FaLock />
                        </div>
                        <h3>Tamamen Güvenli</h3>
                        <p>Verileriniz uçtan uca şifreleme ile korunur.</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default WelcomePage;
