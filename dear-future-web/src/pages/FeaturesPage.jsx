import './FeaturesPage.css';
import { FaLock, FaClock, FaVideo, FaEnvelopeOpenText } from 'react-icons/fa';

const FeaturesPage = () => {
    const features = [
        {
            icon: <FaClock />,
            title: 'Zaman Kapsülleri',
            description: 'Mektuplarınızı, fotoğraflarınızı ve videolarınızı yıllar sonrasına zamanlayın. Doğum günleri, yıldönümleri veya gelecekteki siz için notlar bırakın.',
        },
        {
            icon: <FaLock />,
            title: 'Uçtan Uca Şifreleme',
            description: 'Anılarınız size ait. Gelişmiş şifreleme ile mesajlarınızı yalnızca sizin belirlediğiniz kişiler okuyabilir.',
        },
        {
            icon: <FaVideo />,
            title: 'Zengin İçerik Desteği',
            description: 'Sadece yazı yazmayın. Fotoğraf ekleyin, ses kaydı bırakın veya video çekerek duyguyu tam haliyle geleceğe taşıyın.',
        },
        {
            icon: <FaEnvelopeOpenText />,
            title: 'Teslimat Bildirimi',
            description: 'Mektubunuz iletildiğinde ve açıldığında haberdar olun; geleceğe bıraktığınız iz yerine ulaştığında bilginiz olsun.',
        },
    ];

    return (
        <section className="features-container">
            <div className="features-inner">
                <header className="features-hero">
                    <span className="features-pill">Özellikler</span>
                    <h1>Geleceğe yazarken ihtiyacın olan her şey</h1>
                    <p>
                        Dear Future, bugünden yarınlara bırakacağın dijital mektupları güvenle saklaman ve doğru zamanda
                        ulaştırman için tasarlandı.
                    </p>
                </header>

                <div className="features-grid">
                    {features.map((feature, index) => (
                        <article key={index} className="feature-card">
                            <div className="feature-icon">{feature.icon}</div>
                            <h2>{feature.title}</h2>
                            <p>{feature.description}</p>
                        </article>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default FeaturesPage;
