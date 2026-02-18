import { Link, useLocation, useNavigate } from 'react-router-dom';
import './NotFoundPage.css';

const NotFoundPage = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const hasToken = !!localStorage.getItem('token');
  const homePath = hasToken ? '/settings' : '/welcome';

  return (
    <div className="notfound-wrap">
      <div className="notfound-bg" aria-hidden="true" />
      <div className="notfound-container">
        <div className="notfound-card">
          <div className="notfound-hero">
            <span className="notfound-number" aria-hidden="true">404</span>
            <div className="notfound-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 8v4M12 16h.01" />
              </svg>
            </div>
          </div>
          <h1 className="notfound-title">Sayfa bulunamadı</h1>
          <p className="notfound-desc">
            Bu adres mevcut değil veya taşınmış olabilir. Ana sayfaya dönüp devam edebilirsin.
          </p>

          <div className="notfound-url-wrap">
            <code className="notfound-url">{location.pathname}</code>
          </div>

          <div className="notfound-actions">
            <button
              type="button"
              className="notfound-btn notfound-btn-ghost"
              onClick={() => navigate(-1)}
            >
              ← Geri dön
            </button>
            <Link className="notfound-btn notfound-btn-primary" to={homePath}>
              Ana sayfaya git
            </Link>
          </div>

          <nav className="notfound-nav" aria-label="Hızlı linkler">
            <Link to="/features">Özellikler</Link>
            <Link to="/pricing">Fiyatlandırma</Link>
            <Link to="/contact">İletişim</Link>
          </nav>
        </div>
      </div>
    </div>
  );
};

export default NotFoundPage;
