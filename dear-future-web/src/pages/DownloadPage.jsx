import { FaAndroid, FaDownload, FaMobileAlt } from 'react-icons/fa';
import './DownloadPage.css';

const APK_URL = '/dear-future.apk';
const APK_FILENAME = 'dear-future.apk';

const DownloadPage = () => {
  const handleDownload = () => {
    const link = document.createElement('a');
    link.href = APK_URL;
    link.download = APK_FILENAME;
    link.setAttribute('download', APK_FILENAME);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <section className="download-container">
      <div className="download-inner">
        <header className="download-hero">
          <span className="download-pill">Mobil</span>
          <h1>Dear Future Android Uygulaması</h1>
          <p>
            Geleceğe mesajlarınızı telefonunuzdan yazın. APK dosyasını indirip
            Android cihazınıza kurabilirsiniz.
          </p>
        </header>

        <div className="download-card">
          <div className="download-card-icon" aria-hidden="true">
            <FaAndroid />
          </div>
          <h2>Android için İndir</h2>
          <p className="download-card-desc">
            <FaMobileAlt /> Android 5.0 ve üzeri cihazlarla uyumludur.
          </p>
          <a
            href={APK_URL}
            download={APK_FILENAME}
            className="download-btn"
            onClick={handleDownload}
          >
            <FaDownload />
            APK İndir
          </a>
          <p className="download-hint">
            İndirdikten sonra dosyayı açıp kurulumu tamamlayın. “Bilinmeyen kaynaklara izin ver”
            gerekebilir.
          </p>
        </div>

        <div className="download-info">
          <h3>Kurulum adımları</h3>
          <ol>
            <li>Yukarıdaki <strong>APK İndir</strong> butonuna tıklayın.</li>
            <li>İndirilen <code>dear-future.apk</code> dosyasını açın.</li>
            <li>Güvenlik uyarısında “Yine de yükle” / “Bilinmeyen kaynaklara izin ver” seçeneğini onaylayın.</li>
            <li>Kurulum bitince uygulamayı açıp giriş yapın veya hesap oluşturun.</li>
          </ol>
        </div>
      </div>
    </section>
  );
};

export default DownloadPage;
