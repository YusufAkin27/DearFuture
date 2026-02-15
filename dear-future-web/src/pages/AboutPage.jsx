import {
    Accordion,
    AccordionItem,
    AccordionTrigger,
    AccordionPanel,
} from '../components/Accordion';
import './AboutPage.css';

const ABOUT_FAQ = [
    {
        title: 'Dear Future nedir?',
        content: 'Dear Future, kendinize veya sevdiklerinize gelecekte ulaşacak dijital mektuplar, fotoğraflar ve videolar göndermenizi sağlayan bir zaman kapsülü platformudur. Mesajlarınız güvenle saklanır ve belirlediğiniz tarihte ulaştırılır.',
    },
    {
        title: 'Nasıl çalışır?',
        content: 'Kayıt olun, bir mesaj oluşturun ve teslim tarihini seçin. Mesajınız şifrelenir ve güvenle saklanır. Tarih geldiğinde alıcı e-posta ile bilgilendirilir ve mesajına erişebilir.',
    },
    {
        title: 'Ücretsiz mi kullanılır?',
        content: 'Evet. Dear Future belirli bir kotaya kadar ücretsiz kullanım sunar. Daha fazla mesaj veya depolama için ücretli planlarımız mevcuttur. Detaylar için Fiyatlandırma sayfamıza bakabilirsiniz.',
    },
];

const AboutPage = () => {
    return (
        <section className="about-container">
            <div className="about-inner">
                <header className="about-hero">
                    <span className="about-pill">Hakkımızda</span>
                    <h1>Misyonumuz zamanı geleceğe taşımak</h1>
                    <p>
                        Her anın, her düşüncenin ve her duygunun hatırlanmayı hak ettiğine inanıyoruz.
                    </p>
                </header>

                <div className="about-accordion-wrap">
                    <Accordion className="about-accordion">
                        {ABOUT_FAQ.map((item, index) => (
                            <AccordionItem key={index} value={`about-${index + 1}`}>
                                <AccordionTrigger showArrow={true}>
                                    {item.title}
                                </AccordionTrigger>
                                <AccordionPanel>
                                    <p>{item.content}</p>
                                </AccordionPanel>
                            </AccordionItem>
                        ))}
                    </Accordion>
                </div>

                <div className="about-section">
                    <div className="about-content">
                        <h2>Biz Kimiz</h2>
                        <p>
                            Dear Future, basit bir fikirden doğdu: Gelecekle konuşabilseydiniz ne olurdu?
                            Hayal kuranlar, mühendisler ve anı bekçilerinden oluşan ekibimiz, dünyanın en güvenli
                            ve kullanımı kolay dijital zaman kapsülü platformunu inşa etmek için çalışıyor.
                        </p>
                    </div>
                    <div className="about-image placeholder-image" aria-hidden="true" />
                </div>

                <div className="about-section about-section--reverse">
                    <div className="about-content">
                        <h2>Neden Yapıyoruz</h2>
                        <p>
                            Dijital içeriğin bir anda kaybolduğu hızlı bir dünyada, yavaş akan bir sığınak
                            yaratmak istiyoruz. Mesajların hayatın hızında ilerlediği, tam da en çok ihtiyaç
                            duyulduğu anda ulaştığı bir yer.
                        </p>
                    </div>
                    <div className="about-image placeholder-image" aria-hidden="true" />
                </div>

                <div className="about-team">
                    <h2>Ekibimiz</h2>
                    <div className="about-team-grid">
                        {[
                            { id: 1, name: 'Yusuf Akin', role: 'Kurucu Ortağı', image: '/uye1.avif' },
                            { id: 2, name: 'Melisa Çiçek Soyubey', role: 'Kurucu Ortağı', image: '/uye2.avif' },
                        ].map((member) => (
                            <div key={member.id} className="about-team-card">
                                <img
                                    src={member.image}
                                    alt={member.name}
                                    className="about-team-avatar"
                                />
                                <h3>{member.name}</h3>
                                <p>{member.role}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </section>
    );
};

export default AboutPage;
