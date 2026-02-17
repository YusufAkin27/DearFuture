import { useState, useEffect, createContext, useContext } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { FaBars, FaChevronRight, FaChevronLeft, FaSignInAlt, FaSignOutAlt } from 'react-icons/fa';
import './Sidebar.css';

const SIDEBAR_PUBLIC = [
  {
    label: 'Keşfet',
    links: [
      { label: 'Ana Sayfa', to: '/welcome' },
      { label: 'Özellikler', to: '/features' },
      { label: 'Blog', to: '/blog' },
      { label: 'Fiyatlandırma', to: '/pricing' },
    ],
  },
  {
    label: 'Hakkında',
    links: [
      { label: 'Hakkımızda', to: '/about' },
      { label: 'İletişim', to: '/contact' },
    ],
  },
  {
    label: 'Yasal',
    links: [
      { label: 'Gizlilik', to: '/privacy' },
      { label: 'Kullanım Şartları', to: '/terms' },
      { label: 'Çerez Politikası', to: '/cookie-policy' },
    ],
  },
];

const SIDEBAR_PRIVATE = [
  {
    label: 'Mesajlar',
    links: [{ label: 'Yeni Mesaj', to: '/new' }],
  },
  {
    label: 'Hesabım',
    links: [
      { label: 'Profil', to: '/profile' },
      { label: 'Ayarlar', to: '/settings' },
    ],
  },
  {
    label: 'Plan',
    links: [{ label: 'Abonelik', to: '/change-subscription' }],
  },
];

const SidebarContext = createContext({ open: true, setOpen: () => {}, collapsed: false, setCollapsed: () => {} });

export const useSidebar = () => useContext(SidebarContext);

export const SidebarProvider = ({ children }) => {
  const [open, setOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(false);

  return (
    <SidebarContext.Provider value={{ open, setOpen, collapsed, setCollapsed }}>
      {children}
    </SidebarContext.Provider>
  );
};

export const SidebarTrigger = () => {
  const { setOpen } = useSidebar();
  return (
    <button
      type="button"
      className="sidebar-trigger"
      onClick={() => setOpen((o) => !o)}
      aria-label="Menüyü aç/kapat"
    >
      <FaBars />
    </button>
  );
};

export const Sidebar = () => {
  const { open, setOpen, collapsed, setCollapsed } = useSidebar();
  const navigate = useNavigate();
  const location = useLocation();
  const [token, setToken] = useState(() => localStorage.getItem('token'));

  useEffect(() => setToken(localStorage.getItem('token')), [location.pathname]);

  const isLoggedIn = !!token;
  /* Giriş yapınca sadece Mesajlar, Hesabım, Plan. Giriş yoksa Keşfet, Hakkında, Yasal. */
  const groups = isLoggedIn ? SIDEBAR_PRIVATE : SIDEBAR_PUBLIC;

  const handleLogout = () => {
    localStorage.removeItem('token');
    setToken(null);
    navigate('/login');
    setOpen(false);
  };

  const isActive = (path) => location.pathname === path || (path !== '/welcome' && location.pathname.startsWith(path));

  return (
    <>
      <div className={`sidebar-backdrop ${open ? 'sidebar-backdrop--open' : ''}`} onClick={() => setOpen(false)} aria-hidden="true" />
      <aside className={`sidebar ${open ? 'sidebar--open' : ''} ${collapsed ? 'sidebar--collapsed' : ''}`}>
        <div className="sidebar__inner">
          <header className="sidebar-header">
            <Link to={isLoggedIn ? '/' : '/welcome'} className="sidebar-logo" onClick={() => setOpen(false)}>
              <img src="/logo.png" alt="Dear Future" className="sidebar-logo-img" />
              {!collapsed && <span className="sidebar-logo-text">Dear Future</span>}
            </Link>
          </header>

          <nav className="sidebar-content">
            {groups.map((group) => (
              <section key={group.label} className="sidebar-section">
                {!collapsed && <h2 className="sidebar-title">{group.label}</h2>}
                <ul className="sidebar-menu" role="list">
                  {group.links.map((link) => (
                    <li key={link.to}>
                      <Link
                        to={link.to}
                        className={`sidebar-subtitle ${isActive(link.to) ? 'sidebar-subtitle--active' : ''}`}
                        onClick={() => setOpen(false)}
                        title={collapsed ? link.label : undefined}
                      >
                        {!collapsed && (
                          <>
                            <span>{link.label}</span>
                            <FaChevronRight className="sidebar-subtitle-chevron" aria-hidden />
                          </>
                        )}
                        {collapsed && <span className="sidebar-subtitle-icon" aria-hidden />}
                      </Link>
                    </li>
                  ))}
                </ul>
              </section>
            ))}
          </nav>

          <footer className="sidebar-footer">
            {isLoggedIn ? (
              <button type="button" className="sidebar-footer-btn sidebar-footer-btn--logout" onClick={handleLogout}>
                <FaSignOutAlt />
                {!collapsed && <span>Çıkış Yap</span>}
              </button>
            ) : (
              <Link to="/login" className="sidebar-footer-btn sidebar-footer-btn--cta" onClick={() => setOpen(false)}>
                <FaSignInAlt />
                {!collapsed && <span>Giriş Yap</span>}
              </Link>
            )}
          </footer>
        </div>
        <button
          type="button"
          className="sidebar-collapse-btn"
          onClick={() => setCollapsed((c) => !c)}
          aria-label={collapsed ? 'Sidebarı genişlet' : 'Sidebarı daralt'}
          title={collapsed ? 'Genişlet' : 'Daralt'}
        >
          {collapsed ? <FaChevronRight /> : <FaChevronLeft />}
        </button>
      </aside>
    </>
  );
};

export const SidebarInset = ({ children }) => {
  return <div className="sidebar-inset">{children}</div>;
};
