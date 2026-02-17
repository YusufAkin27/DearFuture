import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Outlet, Navigate, useLocation } from 'react-router-dom';
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
import ContactPage from './pages/ContactPage';
import PrivacyPage from './pages/PrivacyPage';
import TermsPage from './pages/TermsPage';
import SecurityPage from './pages/SecurityPage';
import CookiePolicyPage from './pages/CookiePolicyPage';
import PublicMessagesPage from './pages/PublicMessagesPage';
import MessageViewPage from './pages/MessageViewPage';
import CookieConsent from './components/CookieConsent';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const BASE_TITLE = 'Dear Future – Geleceğe Mesaj Yaz';
const PAGE_TITLES = {
  '/welcome': `${BASE_TITLE} | Hoş Geldin`,
  '/login': `Giriş | ${BASE_TITLE}`,
  '/verify': `Doğrulama | ${BASE_TITLE}`,
  '/pricing': `Fiyatlandırma | ${BASE_TITLE}`,
  '/pricing/plan': `Plan detayı | ${BASE_TITLE}`,
  '/features': `Özellikler | ${BASE_TITLE}`,
  '/blog': `Blog | ${BASE_TITLE}`,
  '/about': `Hakkımızda | ${BASE_TITLE}`,
  '/contact': `İletişim | ${BASE_TITLE}`,
  '/privacy': `Gizlilik | ${BASE_TITLE}`,
  '/terms': `Kullanım Koşulları | ${BASE_TITLE}`,
  '/cookie-policy': `Çerez Politikası | ${BASE_TITLE}`,
  '/security': `Güvenlik | ${BASE_TITLE}`,
  '/public-messages': `Herkese Açık Mesajlar | ${BASE_TITLE}`,
  '/new': `Yeni Mesaj Yaz | ${BASE_TITLE}`,
  '/profile': `Profil | ${BASE_TITLE}`,
  '/change-subscription': `Abonelik | ${BASE_TITLE}`,
  '/settings': `Ayarlar | ${BASE_TITLE}`,
};

function DocumentTitle() {
  const location = useLocation();
  useEffect(() => {
    const path = location.pathname
      .replace(/\/message\/view\/[^/]+/, '/message/view')
      .replace(/\/pricing\/plan\/[^/]+/, '/pricing/plan');
    const title = PAGE_TITLES[path] ?? (path.startsWith('/message/view') ? `Mesajı Görüntüle | ${BASE_TITLE}` : BASE_TITLE);
    document.title = title;
  }, [location.pathname]);
  return null;
}

const Layout = () => {
  const location = useLocation();
  const isWelcome = location.pathname === '/welcome';
  return (
    <div className={`app-layout${isWelcome ? ' app-layout--welcome' : ''}`}>
      <SidebarProvider>
        <Sidebar />
        <SidebarInset>
         
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

import { getTheme, setTheme } from './theme';

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
          {/* Public Routes */}
          <Route path="/welcome" element={<WelcomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/auth/callback" element={<AuthCallbackPage />} />
          <Route path="/verify" element={<VerifyPage />} />
          <Route path="/pricing" element={<PricingPage />} />
          <Route path="/pricing/plan/:code" element={<PlanDetailPage />} />

          <Route path="/features" element={<FeaturesPage />} />
          <Route path="/blog" element={<BlogPage />} />

          <Route path="/about" element={<AboutPage />} />
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
              <Route path="/" element={<Navigate to="/settings" replace />} />
              <Route path="/new" element={<NewMessagePage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/change-subscription" element={<ChangeSubscriptionPage />} />
              <Route path="/settings" element={<SettingsPage />} />
            </Route>
          </Route>
        </Route>
      </Routes>
      <ToastContainer position="bottom-right" theme="light" />
    </Router>
  );
}

export default App;
