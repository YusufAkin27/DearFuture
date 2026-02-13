import { useLayoutEffect, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { gsap } from 'gsap';
import { GoArrowUpRight } from 'react-icons/go';
import { FaSun, FaMoon } from 'react-icons/fa';
import './CardNav.css';

const CardNav = ({
  logo,
  logoAlt = 'Logo',
  logoText,
  logoTo = '/welcome',
  items,
  className = '',
  ease = 'power3.out',
  baseColor,
  menuColor,
  buttonBgColor,
  buttonTextColor,
  ctaLabel,
  ctaTo,
  onCtaClick,
  showThemeToggle = false,
  themeDark = false,
  onThemeToggle,
  themeToggleAriaLabel,
  isOnGalaxy = false,
}) => {
  const [isHamburgerOpen, setIsHamburgerOpen] = useState(false);
  const [isExpanded, setIsExpanded] = useState(false);
  const navRef = useRef(null);
  const cardsRef = useRef([]);
  const tlRef = useRef(null);

  const calculateHeight = () => {
    const navEl = navRef.current;
    if (!navEl) return 260;

    const isMobile = window.matchMedia('(max-width: 768px)').matches;
    if (isMobile) {
      const contentEl = navEl.querySelector('.card-nav-content');
      if (contentEl) {
        const wasVisible = contentEl.style.visibility;
        const wasPointerEvents = contentEl.style.pointerEvents;
        const wasPosition = contentEl.style.position;
        const wasHeight = contentEl.style.height;

        contentEl.style.visibility = 'visible';
        contentEl.style.pointerEvents = 'auto';
        contentEl.style.position = 'static';
        contentEl.style.height = 'auto';

        contentEl.offsetHeight;

        const topBar = 60;
        const padding = 16;
        const contentHeight = contentEl.scrollHeight;

        contentEl.style.visibility = wasVisible;
        contentEl.style.pointerEvents = wasPointerEvents;
        contentEl.style.position = wasPosition;
        contentEl.style.height = wasHeight;

        return topBar + contentHeight + padding;
      }
    }
    return 260;
  };

  const createTimeline = () => {
    const navEl = navRef.current;
    if (!navEl) return null;

    gsap.set(navEl, { height: 60, overflow: 'hidden' });
    gsap.set(cardsRef.current, { y: 50, opacity: 0 });

    const tl = gsap.timeline({ paused: true });

    tl.to(navEl, {
      height: calculateHeight,
      duration: 0.4,
      ease
    });

    tl.to(cardsRef.current, { y: 0, opacity: 1, duration: 0.4, ease, stagger: 0.08 }, '-=0.1');

    return tl;
  };

  useEffect(() => {
    if (isExpanded) document.body.classList.add('card-nav-open');
    else document.body.classList.remove('card-nav-open');
    return () => document.body.classList.remove('card-nav-open');
  }, [isExpanded]);

  useLayoutEffect(() => {
    const tl = createTimeline();
    tlRef.current = tl;

    return () => {
      tl?.kill();
      tlRef.current = null;
    };
  }, [ease, items?.length]);

  useLayoutEffect(() => {
    const handleResize = () => {
      if (!tlRef.current) return;

      if (isExpanded) {
        const newHeight = calculateHeight();
        if (navRef.current) gsap.set(navRef.current, { height: newHeight });

        tlRef.current.kill();
        const newTl = createTimeline();
        if (newTl) {
          newTl.progress(1);
          tlRef.current = newTl;
        }
      } else {
        tlRef.current.kill();
        const newTl = createTimeline();
        if (newTl) {
          tlRef.current = newTl;
        }
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [isExpanded]);

  const toggleMenu = () => {
    const tl = tlRef.current;
    if (!tl) return;
    if (!isExpanded) {
      setIsHamburgerOpen(true);
      setIsExpanded(true);
      tl.play(0);
    } else {
      setIsHamburgerOpen(false);
      tl.eventCallback('onReverseComplete', () => setIsExpanded(false));
      tl.reverse();
    }
  };

  const setCardRef = (i) => (el) => {
    if (el) cardsRef.current[i] = el;
  };

  const renderLink = (lnk, onClick) => {
    if (lnk.to) {
      return (
        <Link
          to={lnk.to}
          className="nav-card-link"
          aria-label={lnk.ariaLabel || lnk.label}
          onClick={onClick}
        >
          <GoArrowUpRight className="nav-card-link-icon" aria-hidden="true" />
          {lnk.label}
        </Link>
      );
    }
    return (
      <a
        href={lnk.href || '#'}
        className="nav-card-link"
        aria-label={lnk.ariaLabel || lnk.label}
        onClick={(e) => { onClick(); if (lnk.href?.startsWith('#')) e.preventDefault(); }}
      >
        <GoArrowUpRight className="nav-card-link-icon" aria-hidden="true" />
        {lnk.label}
      </a>
    );
  };

  return (
    <div className={`card-nav-container${isOnGalaxy ? ' card-nav-container--on-galaxy' : ''}${className ? ` ${className}` : ''}`}>
      <nav
        ref={navRef}
        className={`card-nav ${isExpanded ? 'open' : ''}`}
        style={{ backgroundColor: baseColor }}
        role="banner"
      >
        <div className="card-nav-top">
          <div
            className={`hamburger-menu ${isHamburgerOpen ? 'open' : ''}`}
            onClick={toggleMenu}
            role="button"
            aria-label={isExpanded ? 'Menüyü kapat' : 'Menüyü aç'}
            tabIndex={0}
            onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); toggleMenu(); } }}
            style={{ color: menuColor }}
          >
            <div className="hamburger-line" />
            <div className="hamburger-line" />
          </div>

          <Link to={logoTo} className="logo-container" onClick={() => isExpanded && toggleMenu()}>
            <img src={logo} alt={logoAlt} className="card-nav-logo-img" />
            {logoText && <span className="card-nav-logo-text">{logoText}</span>}
          </Link>

          <div className="card-nav-actions">
          
            {ctaTo ? (
              <Link
                to={ctaTo}
                className="card-nav-cta-button"
                style={{ backgroundColor: buttonBgColor, color: buttonTextColor }}
                onClick={() => isExpanded && toggleMenu()}
              >
                {ctaLabel}
              </Link>
            ) : (
              <button
                type="button"
                className="card-nav-cta-button"
                style={{ backgroundColor: buttonBgColor, color: buttonTextColor }}
                onClick={() => { onCtaClick?.(); isExpanded && toggleMenu(); }}
              >
                {ctaLabel}
              </button>
            )}
          </div>
        </div>

        <div className="card-nav-content" aria-hidden={!isExpanded}>
          {(items || []).slice(0, 3).map((item, idx) => (
            <div
              key={`${item.label}-${idx}`}
              className="nav-card"
              ref={setCardRef(idx)}
              style={{ backgroundColor: item.bgColor, color: item.textColor }}
            >
              <div className="nav-card-label">{item.label}</div>
              <div className="nav-card-links">
                {item.links?.map((lnk, i) => (
                  <span key={`${lnk.label}-${i}`}>
                    {renderLink(lnk, toggleMenu)}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      </nav>
    </div>
  );
};

export default CardNav;
