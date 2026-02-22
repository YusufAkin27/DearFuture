import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Outlet, useLocation } from 'react-router-dom';
import { SidebarProvider, Sidebar, SidebarInset, SidebarTrigger } from './components/Sidebar';
import Footer from './components/Footer';
import PrivateRoute from './components/PrivateRoute';
import LoginPage from './pages/LoginPage';
import AuthCallbackPage from './pages/AuthCallbackPage';
import WelcomePage from './pages/WelcomePage';
import VerifyPage from './pages/VerifyPage';
import NewMessagePage from './pages/NewMessagePage';
import ProfilePage from './pages/ProfilePage';
import PricingPage from './pages/PricingPage';
import PlanDetailPage from './pages/PlanDetailPage';
import ChangeSubscriptionPage from './pages/ChangeSubscriptionPage';
import SettingsPage from './pages/SettingsPage';
import FeaturesPage from './pages/FeaturesPage';
import BlogPage from './pages/BlogPage';
import AboutPage from './pages/AboutPage';
import TeamMemberPage from './pages/TeamMemberPage';
import SSSPage from './pages/SSSPage';
import ContactPage from './pages/ContactPage';
import PrivacyPage from './pages/PrivacyPage';
import TermsPage from './pages/TermsPage';
import SecurityPage from './pages/SecurityPage';
import CookiePolicyPage from './pages/CookiePolicyPage';
import PublicMessagesPage from './pages/PublicMessagesPage';
import MessageViewPage from './pages/MessageViewPage';
import NotFoundPage from './pages/NotFoundPage';
import OAuth2Redirect from './components/OAuth2Redirect';
import CookieConsent from './components/CookieConsent';
import { getTheme, setTheme } from './theme';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const BASE_TITLE = 'Dear Future \u2013 Gelece\u011fe Mesaj Yaz';
const PAGE_TITLES = {
  '/': `${BASE_TITLE} | Ho\u015f Geldin`,
  '/welcome': `${BASE_TITLE} | Ho\u015f Geldin`,
  '/login': `Giri\u015f | ${BASE_TITLE}`,
  '/verify': `Do\u011frulama | ${BASE_TITLE}`,
  '/pricing': `Fiyatland\u0131rma | ${BASE_TITLE}`,
  '/pricing/plan': `Plan detay\u0131 | ${BASE_TITLE}`,
  '/features': `\u00d6zellikler | ${BASE_TITLE}`,
  '/blog': `Blog | ${BASE_TITLE}`,
  '/about': `Hakk\u0131m\u0131zda | ${BASE_TITLE}`,
  '/about/team': `Kurucu Orta\u011f\u0131 | ${BASE_TITLE}`,
  '/sss': `S\u0131k\u00e7a Sorulan Sorular | ${BASE_TITLE}`,
  '/contact': `\u0130leti\u015fim | ${BASE_TITLE}`,
  '/privacy': `Gizlilik | ${BASE_TITLE}`,
  '/terms': `Kullan\u0131m Ko\u015fullar\u0131 | ${BASE_TITLE}`,
  '/cookie-policy': `\u00c7erez Politikas\u0131 | ${BASE_TITLE}`,
  '/security': `G\u00fcvenlik | ${BASE_TITLE}`,
  '/public-messages': `Herkese A\u00e7\u0131k Mesajlar | ${BASE_TITLE}`,
  '/new': `Yeni Mesaj Yaz | ${BASE_TITLE}`,
  '/profile': `Profil | ${BASE_TITLE}`,
  '/change-subscription': `Abonelik | ${BASE_TITLE}`,
  '/settings': `Ayarlar | ${BASE_TITLE}`,
};

function DocumentTitle() {
  const location = useLocation();
  useEffect(() => {
    let path = location.pathname
      .replace(/\/message\/view\/[^/]+/, '/message/view')
      .replace(/\/pricing\/plan\/[^/]+/, '/pricing/plan');
    if (path.startsWith('/about/team/')) path = '/about/team';
    const title = PAGE_TITLES[path] ?? (path.startsWith('/message/view') ? `Mesaj\u0131 G\u00f6r\u00fcnt\u00fcle | ${BASE_TITLE}` : BASE_TITLE);
    document.title = title;
  }, [location.pathname]);
  return null;
}

const Layout = () => {
  const location = useLocation();
  const isWelcome = location.pathname === '/' || location.pathname === '/welcome';
  return (
    <div className={`app-layout${isWelcome ? ' app-layout--welcome' : ''}`}>
      <SidebarProvider>
        <Sidebar />
        <SidebarInset>
          <header className="app-topbar" role="banner">
            <SidebarTrigger />
            <span className="app-topbar-title">
              {location.pathname === '/welcome' && 'Ana Sayfa'}
              {location.pathname === '/features' && '\u00d6zellikler'}
              {location.pathname === '/pricing' && 'Fiyatland\u0131rma'}
              {location.pathname === '/blog' && 'Blog'}
              {location.pathname === '/about' && 'Hakk\u0131m\u0131zda'}
              {location.pathname.startsWith('/about/team') && 'Kurucu Orta\u011f\u0131'}
              {location.pathname === '/sss' && 'S\u0131k\u00e7a Sorulan Sorular'}
              {location.pathname === '/contact' && '\u0130leti\u015fim'}
              {location.pathname === '/login' && 'Giri\u015f'}
              {location.pathname === '/verify' && 'Do\u011frulama'}
              {location.pathname.startsWith('/public-messages') && 'Herkese A\u00e7\u0131k Mesajlar'}
              {location.pathname === '/new' && 'Yeni Mesaj'}
              {location.pathname === '/profile' && 'Profil'}
              {location.pathname === '/settings' && 'Ayarlar'}
              {location.pathname === '/change-subscription' && 'Abonelik'}
              {location.pathname === '/privacy' && 'Gizlilik'}
              {location.pathname === '/terms' && 'Kullan\u0131m \u015eartlar\u0131'}
              {location.pathname === '/cookie-policy' && '\u00c7erez Politikas\u0131'}
              {location.pathname === '/security' && 'G\u00fcvenlik'}
              {location.pathname.startsWith('/message/view') && 'Mesaj'}
              {location.pathname.startsWith('/pricing/plan') && 'Plan'}
              {location.pathname === '/' && 'Ana Sayfa'}
              {!['/welcome', '/features', '/pricing', '/blog', '/about', '/sss', '/contact', '/login', '/verify', '/new', '/profile', '/settings', '/change-subscription', '/privacy', '/terms', '/cookie-policy', '/security', '/'].includes(location.pathname) && !location.pathname.startsWith('/public-messages') && !location.pathname.startsWith('/message/view') && !location.pathname.startsWith('/pricing/plan') && !location.pathname.startsWith('/about/team') && 'Men\u00fc'}
            </span>
          </header>
          <main className="app-main">
            <Outlet />
          </main>
          <Footer />
        </SidebarInset>
      </SidebarProvider>
    </div>
  );
};

const ProtectedLayout = () => (
  <div className="app-protected">
    <Outlet />
  </div>
);

function App() {
  useEffect(() => {
    setTheme(getTheme());
  }, []);

  return (
    <Router>
      <DocumentTitle />
      <CookieConsent />
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<WelcomePage />} />
          <Route path="/welcome" element={<WelcomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/oauth2/*" element={<OAuth2Redirect />} />
          <Route path="/auth/callback" element={<AuthCallbackPage />} />
          <Route path="/verify" element={<VerifyPage />} />
          <Route path="/pricing" element={<PricingPage />} />
          <Route path="/pricing/plan/:code" element={<PlanDetailPage />} />

          <Route path="/features" element={<FeaturesPage />} />
          <Route path="/blog" element={<BlogPage />} />

          <Route path="/about" element={<AboutPage />} />
          <Route path="/about/team/:slug" element={<TeamMemberPage />} />
          <Route path="/sss" element={<SSSPage />} />
          <Route path="/contact" element={<ContactPage />} />

          <Route path="/privacy" element={<PrivacyPage />} />
          <Route path="/terms" element={<TermsPage />} />
          <Route path="/cookie-policy" element={<CookiePolicyPage />} />
          <Route path="/security" element={<SecurityPage />} />
          <Route path="/public-messages" element={<PublicMessagesPage />} />
          <Route path="/message/view/:viewToken" element={<MessageViewPage />} />

          {/* Protected Routes */}
          <Route element={<PrivateRoute />}>
            <Route element={<ProtectedLayout />}>
              <Route path="/new" element={<NewMessagePage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/change-subscription" element={<ChangeSubscriptionPage />} />
              <Route path="/settings" element={<SettingsPage />} />
            </Route>
          </Route>

          {/* 404 */}
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
      <ToastContainer position="bottom-right" theme="light" />
    </Router>
  );
}

export default App;
