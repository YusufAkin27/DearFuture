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
import DeliveredMessagesPage from './pages/DeliveredMessagesPage';
import AccountDangerPage from './pages/AccountDangerPage';
import FeaturesPage from './pages/FeaturesPage';
import BlogPage from './pages/BlogPage';
import AboutPage from './pages/AboutPage';
import TeamMemberPage from './pages/TeamMemberPage';
import SSSPage from './pages/SSSPage';
import ContactPage from './pages/ContactPage';
import DownloadPage from './pages/DownloadPage';
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
const BASE_URL = (import.meta.env.VITE_APP_URL || '').replace(/\/$/, '') || (typeof window !== 'undefined' ? window.location.origin : 'https://dearfuture.com.tr');

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
  '/download': `Android \u0130ndir | ${BASE_TITLE}`,
  '/privacy': `Gizlilik | ${BASE_TITLE}`,
  '/terms': `Kullan\u0131m Ko\u015fullar\u0131 | ${BASE_TITLE}`,
  '/cookie-policy': `\u00c7erez Politikas\u0131 | ${BASE_TITLE}`,
  '/security': `G\u00fcvenlik | ${BASE_TITLE}`,
  '/public-messages': `Herkese A\u00e7\u0131k Mesajlar | ${BASE_TITLE}`,
  '/new': `Yeni Mesaj Yaz | ${BASE_TITLE}`,
  '/profile': `Profil | ${BASE_TITLE}`,
  '/change-subscription': `Abonelik | ${BASE_TITLE}`,
  '/settings': `Ayarlar | ${BASE_TITLE}`,
  '/settings/delivered-messages': `\u0130letilen Mesajlar | ${BASE_TITLE}`,
  '/settings/account-danger': `Hesap Dondurma ve Silme | ${BASE_TITLE}`,
};

const DEFAULT_DESCRIPTION = 'Geleceğe mesaj yaz, zamanlanmış mektup bırak. Kendinize veya sevdiklerinize dijital zaman kapsülü oluşturun; metin, fotoğraf ve ses kaydı ile gelecekteki anı yaşatın.';

const PAGE_DESCRIPTIONS = {
  '/': DEFAULT_DESCRIPTION,
  '/welcome': DEFAULT_DESCRIPTION,
  '/login': 'Dear Future hesabınıza giriş yapın veya kayıt olun. Geleceğe mesaj yazmaya hemen başlayın.',
  '/verify': 'E-posta veya telefon doğrulama sayfası.',
  '/pricing': 'Dear Future fiyatlandırma planları. Ücretsiz ve premium seçeneklerle geleceğe mesaj yazın.',
  '/pricing/plan': 'Plan detayları ve özellikler.',
  '/features': 'Zaman kapsülü, zamanlanmış mesaj, fotoğraf ve video ile geleceğe mektup özellikleri.',
  '/blog': 'Geleceğe mektup, dijital miras ve zaman kapsülü hakkında yazılar ve ipuçları.',
  '/about': 'Dear Future hakkında: misyonumuz zamanı geleceğe taşımak.',
  '/about/team': 'Dear Future kurucu ekibi.',
  '/sss': 'Sıkça sorulan sorular: geleceğe mesaj, zaman kapsülü, güvenlik ve abonelik.',
  '/contact': 'Dear Future iletişim: bize ulaşın, destek ve geri bildirim.',
  '/download': 'Dear Future Android uygulamasını indirin. Geleceğe mesaj yazın.',
  '/privacy': 'Dear Future gizlilik politikası ve kişisel verilerin korunması.',
  '/terms': 'Dear Future kullanım koşulları.',
  '/cookie-policy': 'Dear Future çerez politikası.',
  '/security': 'Dear Future güvenlik: şifreleme ve veri koruma.',
  '/public-messages': 'Herkese açık geleceğe mesajlar. Topluluk zaman kapsülleri.',
  '/new': 'Yeni mesaj yaz: geleceğe mektup, fotoğraf veya ses ekleyin.',
  '/profile': 'Profilinizi ve mesajlarınızı yönetin.',
  '/change-subscription': 'Aboneliğinizi yönetin veya plan değiştirin.',
  '/settings': 'Hesap ve uygulama ayarları.',
  '/settings/delivered-messages': 'Teslim edilmiş mesajlarınızı görüntüleyin.',
  '/settings/account-danger': 'Hesabı dondurma veya kalıcı silme işlemleri.',
};

function DocumentHead() {
  const location = useLocation();
  useEffect(() => {
    let path = location.pathname
      .replace(/\/message\/view\/[^/]+/, '/message/view')
      .replace(/\/pricing\/plan\/[^/]+/, '/pricing/plan');
    if (path.startsWith('/about/team/')) path = '/about/team';
    const title = PAGE_TITLES[path] ?? (path.startsWith('/message/view') ? `Mesaj\u0131 G\u00f6r\u00fcnt\u00fcle | ${BASE_TITLE}` : BASE_TITLE);
    const description = PAGE_DESCRIPTIONS[path] ?? (path.startsWith('/message/view') ? 'Zaman kapsülü mesajını görüntüleyin.' : DEFAULT_DESCRIPTION);
    const origin = typeof window !== 'undefined' ? window.location.origin : BASE_URL;
    const canonicalPath = path === '/' ? '' : path;
    const canonical = `${origin}${canonicalPath || '/'}`.replace(/([^/])\/$/, '$1');

    document.title = title;
    const metaDesc = document.querySelector('meta[name="description"]');
    if (metaDesc) metaDesc.setAttribute('content', description);
    const ogDesc = document.querySelector('meta[property="og:description"]');
    if (ogDesc) ogDesc.setAttribute('content', description);
    const ogTitle = document.querySelector('meta[property="og:title"]');
    if (ogTitle) ogTitle.setAttribute('content', title);
    const ogUrl = document.querySelector('meta[property="og:url"]');
    if (ogUrl) ogUrl.setAttribute('content', canonical);

    let linkCanonical = document.querySelector('link[rel="canonical"]');
    if (!linkCanonical) {
      linkCanonical = document.createElement('link');
      linkCanonical.rel = 'canonical';
      document.head.appendChild(linkCanonical);
    }
    linkCanonical.href = canonical;
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
              {location.pathname === '/download' && 'Android \u0130ndir'}
              {location.pathname === '/login' && 'Giri\u015f'}
              {location.pathname === '/verify' && 'Do\u011frulama'}
              {location.pathname.startsWith('/public-messages') && 'Herkese A\u00e7\u0131k Mesajlar'}
              {location.pathname === '/new' && 'Yeni Mesaj'}
              {location.pathname === '/profile' && 'Profil'}
              {location.pathname === '/settings' && 'Ayarlar'}
              {location.pathname === '/settings/delivered-messages' && '\u0130letilen Mesajlar'}
              {location.pathname === '/settings/account-danger' && 'Hesap Dondurma ve Silme'}
              {location.pathname === '/change-subscription' && 'Abonelik'}
              {location.pathname === '/privacy' && 'Gizlilik'}
              {location.pathname === '/terms' && 'Kullan\u0131m \u015eartlar\u0131'}
              {location.pathname === '/cookie-policy' && '\u00c7erez Politikas\u0131'}
              {location.pathname === '/security' && 'G\u00fcvenlik'}
              {location.pathname.startsWith('/message/view') && 'Mesaj'}
              {location.pathname.startsWith('/pricing/plan') && 'Plan'}
              {location.pathname === '/' && 'Ana Sayfa'}
              {!['/welcome', '/features', '/pricing', '/blog', '/about', '/sss', '/contact', '/download', '/login', '/verify', '/new', '/profile', '/settings', '/change-subscription', '/privacy', '/terms', '/cookie-policy', '/security', '/'].includes(location.pathname) && !location.pathname.startsWith('/public-messages') && !location.pathname.startsWith('/message/view') && !location.pathname.startsWith('/pricing/plan') && !location.pathname.startsWith('/about/team') && 'Men\u00fc'}
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
      <DocumentHead />
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
          <Route path="/download" element={<DownloadPage />} />

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
              <Route path="/settings/delivered-messages" element={<DeliveredMessagesPage />} />
              <Route path="/settings/account-danger" element={<AccountDangerPage />} />
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
