import { useNavigate } from 'react-router-dom';
import { FaArrowRight, FaThLarge } from 'react-icons/fa';
import Galaxy from '../components/Galaxy';
import './WelcomePage.css';

const WelcomePage = () => {
    const navigate = useNavigate();
    const isLoggedIn = !!localStorage.getItem('token');

    const handleStart = () => {
        navigate('/login');
    };

    const handleGoSettings = () => {
        navigate('/settings');
    };

    return (
        <div className="welcome-page">
            <div className="welcome-galaxy-wrap">
                <Galaxy
                    mouseInteraction={false}
                    density={1}
                    glowIntensity={0.3}
                    saturation={0}
                    hueShift={140}
                    twinkleIntensity={0.3}
                    rotationSpeed={0.1}
                    repulsionStrength={2}
                    autoCenterRepulsion={0}
                    starSpeed={0.5}
                    speed={1}
                />
            </div>
            <div className="welcome-container">
                <div className="hero-content">
         
             

                    <h1 className="welcome-title">
                        Bugünden Geleceğe,<br />
                        <span className="gradient-text">Bir Mesajın Var</span>
                    </h1>

                    <p className="welcome-message">
                        Kendinize, sevdiklerinize veya çocuklarınıza yıllar sonra ulaşacak dijital mektuplar,
                        fotoğraflar ve videolar saklayın.
                    </p>

                    {isLoggedIn ? (
                        <button className="cta-button" onClick={handleGoSettings}>
                            <span>Ayarlara Git</span>
                            <FaThLarge className="button-icon" />
                        </button>
                    ) : (
                        <button className="cta-button" onClick={handleStart}>
                            <span>Hemen Başla</span>
                            <FaArrowRight className="button-icon" />
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default WelcomePage;
