import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import CardNav from './CardNav';
import { getTheme, toggleTheme } from '../theme';

const PUBLIC_ITEMS = [
  {
    label: 'Keşfet',
    bgColor: '#0D0716',
    textColor: '#fff',
    links: [
      { label: 'Ana Sayfa', to: '/welcome', ariaLabel: 'Ana sayfa' },
      { label: 'Özellikler', to: '/features', ariaLabel: 'Özellikler' },
      { label: 'Blog', to: '/blog', ariaLabel: 'Blog' },
      { label: 'Fiyatlandırma', to: '/pricing', ariaLabel: 'Fiyatlandırma' }
    ]
  },
  {
    label: 'Hakkında',
    bgColor: '#170D27',
    textColor: '#fff',
    links: [
      { label: 'Hakkımızda', to: '/about', ariaLabel: 'Hakkımızda' },
      { label: 'İletişim', to: '/contact', ariaLabel: 'İletişim' }
    ]
  },
  {
    label: 'Yasal',
    bgColor: '#271E37',
    textColor: '#fff',
    links: [
      { label: 'Gizlilik', to: '/privacy', ariaLabel: 'Gizlilik politikası' },
      { label: 'Kullanım Şartları', to: '/terms', ariaLabel: 'Kullanım şartları' },
      { label: 'Çerez Politikası', to: '/cookie-policy', ariaLabel: 'Çerez politikası' }
    ]
  }
];

const PRIVATE_ITEMS = [
  {
    label: 'Mesajlar',
    bgColor: '#0D0716',
    textColor: '#fff',
    links: [
      { label: 'Yeni Mesaj', to: '/new', ariaLabel: 'Yeni mesaj yaz' }
    ]
  },
  {
    label: 'Hesabım',
    bgColor: '#170D27',
    textColor: '#fff',
    links: [
      { label: 'Profil', to: '/profile', ariaLabel: 'Profil' },
      { label: 'Ayarlar', to: '/settings', ariaLabel: 'Ayarlar' }
    ]
  },
  {
    label: 'Plan',
    bgColor: '#271E37',
    textColor: '#fff',
    links: [
      { label: 'Abonelik', to: '/change-subscription', ariaLabel: 'Abonelik planı' }
    ]
  }
];

const Header = () => {
  const [dark, setDark] = useState(() => getTheme() === 'dark');
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    setToken(localStorage.getItem('token'));
  }, [location.pathname]);

  useEffect(() => {
    setDark(getTheme() === 'dark');
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    setToken(null);
    navigate('/login');
  };

  const handleThemeToggle = () => {
    const next = toggleTheme();
    setDark(next === 'dark');
  };

  const isLoggedIn = !!token;
  const items = isLoggedIn ? PRIVATE_ITEMS : PUBLIC_ITEMS;
  const isWelcomePage = location.pathname === '/welcome';

  const baseColor = 'transparent';
  const menuColor = 'rgba(255, 255, 255, 0.92)';
  const buttonBg = 'var(--primary-color)';
  const buttonText = '#fff';

  return (
    <CardNav
      logo="/logo.png"
      logoAlt="Dear Future"
      logoText="Dear Future"
      logoTo={isLoggedIn ? '/' : '/welcome'}
      items={items}
      baseColor={baseColor}
      menuColor={menuColor}
      buttonBgColor={buttonBg}
      buttonTextColor={buttonText}
      ctaLabel={isLoggedIn ? 'Çıkış Yap' : 'Giriş Yap'}
      ctaTo={isLoggedIn ? undefined : '/login'}
      onCtaClick={isLoggedIn ? handleLogout : undefined}
      showThemeToggle={!isWelcomePage}
      themeDark={dark}
      onThemeToggle={handleThemeToggle}
      themeToggleAriaLabel={dark ? 'Açık tema' : 'Koyu tema'}
      ease="power3.out"
    />
  );
};

export default Header;
