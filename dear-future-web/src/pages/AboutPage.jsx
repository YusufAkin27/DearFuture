import { Link } from 'react-router-dom';
import {
    Accordion,
    AccordionItem,
    AccordionTrigger,
    AccordionPanel,
} from '../components/Accordion';
import { TEAM_MEMBERS } from '../data/teamMembers';
import './AboutPage.css';

 
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
                        {TEAM_MEMBERS.map((member) => (
                            <Link
                                key={member.id}
                                to={`/about/team/${member.slug}`}
                                className="about-team-card about-team-card--link"
                            >
                                <img
                                    src={member.image}
                                    alt={member.name}
                                    className="about-team-avatar"
                                />
                                <h3>{member.name}</h3>
                                <p>{member.role}</p>
                                <span className="about-team-card-hint">Özgeçmiş ve projeler →</span>
                            </Link>
                        ))}
                    </div>
                </div>
            </div>
        </section>
    );
};

export default AboutPage;
