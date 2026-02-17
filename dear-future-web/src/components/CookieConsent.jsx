import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FaCookieBite, FaChevronDown, FaCheck } from 'react-icons/fa';
import { useDebouncedCallback } from '../hooks/useDebouncedCallback';
import './CookieConsent.css';

const STORAGE_KEY = 'dearfuture_cookie_consent';

export const getCookieConsent = () => {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
};

const CookieConsent = () => {
  const [visible, setVisible] = useState(false);
  const [showDetails, setShowDetails] = useState(false);
  const [preferences, setPreferences] = useState({
    essential: true,
    analytics: true,
    marketing: false,
  });

  useEffect(() => {
    const saved = getCookieConsent();
    if (!saved) setVisible(true);
  }, []);

  const save = (value) => {
    try {
      localStorage.setItem(STORAGE_KEY, value);
      setVisible(false);
    } catch (e) {
      console.warn('Cookie consent save failed', e);
    }
  };

  const handleAcceptAll = () => save('accepted_all');
  const handleEssentialOnly = () => save('essential_only');

  const handleSavePreferences = () => {
    const prefs = JSON.stringify(preferences);
    try {
      localStorage.setItem(STORAGE_KEY, 'custom');
      localStorage.setItem(STORAGE_KEY + '_prefs', prefs);
      setVisible(false);
    } catch (e) {
      console.warn('Cookie preferences save failed', e);
    }
  };

  const debouncedSavePreferences = useDebouncedCallback(handleSavePreferences, 500);
  const debouncedAcceptAll = useDebouncedCallback(handleAcceptAll, 500);
  const debouncedEssentialOnly = useDebouncedCallback(handleEssentialOnly, 500);

  const togglePreference = (key) => {
    setPreferences((p) => ({ ...p, [key]: !p[key] }));
  };

  if (!visible) return null;

  return (
    <div
      className="cookie-consent-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="cookie-consent-title"
      aria-describedby="cookie-consent-desc"
    >
      <div className="cookie-consent-modal">
        <div className="cookie-consent-header">
          <span className="cookie-consent-icon" aria-hidden>
            <FaCookieBite />
          </span>
          <h2 id="cookie-consent-title" className="cookie-consent-title">
            Çerez Tercihleri
          </h2>
          <p id="cookie-consent-desc" className="cookie-consent-desc">
            Deneyiminizi iyileştirmek için çerezler kullanıyoruz. Hangi çerezleri kabul etmek istediğinizi seçebilir veya tümünü kabul edebilirsiniz. Detaylar için{' '}
            <Link to="/cookie-policy" className="cookie-consent-link" onClick={() => setVisible(false)}>
              Çerez Politikamızı
            </Link>{' '}
            inceleyebilirsiniz.
          </p>
        </div>

        <button
          type="button"
          className="cookie-consent-details-toggle"
          onClick={() => setShowDetails((d) => !d)}
          aria-expanded={showDetails}
        >
          <span>Çerez türlerini yönet</span>
          <FaChevronDown className={`cookie-consent-chevron ${showDetails ? 'cookie-consent-chevron--open' : ''}`} />
        </button>

        {showDetails && (
          <div className="cookie-consent-details">
            <div className="cookie-consent-row">
              <span className="cookie-consent-row-label">
                <strong>Zorunlu çerezler</strong>
                <small>Site işlevselliği için gerekli, kapatılamaz.</small>
              </span>
              <span className="cookie-consent-badge cookie-consent-badge--always">Her zaman açık</span>
            </div>
            <div className="cookie-consent-row">
              <span className="cookie-consent-row-label">
                <strong>Analitik çerezler</strong>
                <small>Anonim kullanım istatistikleri.</small>
              </span>
              <button
                type="button"
                className={`cookie-consent-toggle ${preferences.analytics ? 'cookie-consent-toggle--on' : ''}`}
                onClick={() => togglePreference('analytics')}
                aria-pressed={preferences.analytics}
              >
                <span className="cookie-consent-toggle-slider" />
              </button>
            </div>
            <div className="cookie-consent-row">
              <span className="cookie-consent-row-label">
                <strong>Pazarlama çerezleri</strong>
                <small>Kişiselleştirilmiş içerik ve reklamlar.</small>
              </span>
              <button
                type="button"
                className={`cookie-consent-toggle ${preferences.marketing ? 'cookie-consent-toggle--on' : ''}`}
                onClick={() => togglePreference('marketing')}
                aria-pressed={preferences.marketing}
              >
                <span className="cookie-consent-toggle-slider" />
              </button>
            </div>
          </div>
        )}

        <div className="cookie-consent-actions">
          {showDetails ? (
            <button type="button" className="cookie-consent-btn cookie-consent-btn--primary" onClick={debouncedSavePreferences}>
              <FaCheck /> Tercihleri Kaydet
            </button>
          ) : (
            <>
              <button type="button" className="cookie-consent-btn cookie-consent-btn--secondary" onClick={debouncedEssentialOnly}>
                Sadece Zorunlu
              </button>
              <button type="button" className="cookie-consent-btn cookie-consent-btn--primary" onClick={debouncedAcceptAll}>
                Tümünü Kabul Et
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default CookieConsent;
