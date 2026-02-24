import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import './AdminLayout.css';

const NAV = [
  { to: '/', label: 'Dashboard' },
  { to: '/users', label: 'Kullanıcılar' },
  { to: '/messages', label: 'Mesajlar' },
  { to: '/payments', label: 'Ödemeler' },
  { to: '/plans', label: 'Planlar' },
  { to: '/contact-messages', label: 'İletişim Mesajları' },
  { to: '/contracts', label: 'Sözleşmeler' },
];

export default function AdminLayout() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('adminToken');
    navigate('/login', { replace: true });
    window.location.reload();
  };

  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <div className="admin-sidebar-title">Dear Future Admin</div>
        <nav className="admin-nav">
          {NAV.map(({ to, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) => `admin-nav-link ${isActive ? 'admin-nav-link--active' : ''}`}
              end={to === '/'}
            >
              {label}
            </NavLink>
          ))}
        </nav>
        <button type="button" className="admin-sidebar-logout" onClick={handleLogout}>
          Çıkış
        </button>
      </aside>
      <div className="admin-content">
        <main className="admin-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
