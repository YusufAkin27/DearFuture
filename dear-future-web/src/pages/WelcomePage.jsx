import { useNavigate } from 'react-router-dom';
import { FaArrowRight, FaThLarge, FaEnvelope, FaLock, FaCalendarAlt } from 'react-icons/fa';
import './WelcomePage.css';

const WelcomePage = () => {
    const navigate = useNavigate();
    const isLoggedIn = !!localStorage.getItem('token');

    const handleStart = () => navigate('/login');
    const handleGoSettings = () => navigate('/settings');

    const features = [
        { icon: FaEnvelope, text: 'Metin, fotoğraf ve video' },
        { icon: FaLock, text: 'Gizli ve güvenli' },
        { icon: FaCalendarAlt, text: 'İstediğin tarihte ulaşır' },
    ];

    return (
        <div className="welcome-page" role="main" aria-label="Hoş geldiniz">
            <div className="welcome-page__bg" aria-hidden="true" />
            <div className="welcome-container">
                <div className="hero-content">
                    <h1 className="welcome-title">
                        Bugünden geleceğe,
                        <br />
                        <span className="welcome-title-accent">bir mesajın var</span>
                    </h1>
                    <p className="welcome-message">
                        Kendinize, sevdiklerinize veya çocuklarınıza yıllar sonra ulaşacak
                        dijital mektuplar, fotoğraflar ve videolar saklayın.
                    </p>
                    {isLoggedIn ? (
                        <button
                            type="button"
                            className="welcome-cta"
                            onClick={handleGoSettings}
                            aria-label="Ayarlara git"
                        >
                            <span>Ayarlara git</span>
                            <FaThLarge className="welcome-cta__icon" aria-hidden="true" />
                        </button>
                    ) : (
                        <button
                            type="button"
                            className="welcome-cta"
                            onClick={handleStart}
                            aria-label="Hemen başla"
                        >
                            <span>Hemen başla</span>
                            <FaArrowRight className="welcome-cta__icon" aria-hidden="true" />
                        </button>
                    )}
                </div>
                <ul className="welcome-features" role="list">
                    {features.map(({ icon: Icon, text }) => (
                        <li key={text} className="welcome-features__item">
                            <span className="welcome-features__icon" aria-hidden="true">
                                <Icon />
                            </span>
                            <span className="welcome-features__text">{text}</span>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default WelcomePage;
