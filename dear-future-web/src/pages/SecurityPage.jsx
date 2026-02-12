import './LegalPage.css';

const SecurityPage = () => {
    return (
        <section className="legal-container">
            <div className="legal-inner">
                <header className="legal-hero">
                    <span className="legal-pill">Güvenlik</span>
                    <h1>Güvenlik</h1>
                    <p className="legal-updated">Son güncelleme: 1 Şubat 2026</p>
                </header>

                <div className="legal-content">
                    <section>
                        <h2>Güvenlik Mimarimiz</h2>
                        <p>
                            Dear Future’da güvenlik sonradan düşünülen bir özellik değil; platformumuzun
                            temelidir. Dijital mirasınızın onlarca yıl güvende kalması için sektörde öncü
                            uygulamalar kullanıyoruz.
                        </p>
                    </section>

                    <section>
                        <h2>Sıfır-Bilgi Şifreleme</h2>
                        <p>
                            Tüm mesaj içerikleri, iletilmeden önce istemci tarafında AES-256 GCM ile
                            şifrelenir. Şifreyi çözmek için gerekli anahtara yalnızca belirlenen alıcı
                            sahiptir. Dear Future çalışanları mesajlarınızı okuyamaz.
                        </p>
                    </section>

                    <section>
                        <h2>Altyapı Güvenliği</h2>
                        <p>
                            Sunucularımız güvenli, ISO 27001 sertifikalı veri merkezlerinde barındırılmaktadır.
                            Sıkı erişim kontrolleri, güvenlik duvarları ve düzenli güvenlik denetimleri
                            uyguluyoruz.
                        </p>
                    </section>

                    <section>
                        <h2>Veri Bütünlüğü</h2>
                        <p>
                            Verilerinizin saklama veya aktarım sırasında değiştirilmediğinden emin olmak için
                            kriptografik özet (hash) kullanıyoruz.
                        </p>
                    </section>

                    <section>
                        <h2>Güvenlik Açığı Bildirimi</h2>
                        <p>
                            Bir güvenlik açığı bulduğunuzu düşünüyorsanız lütfen güvenlik@dearfuture.com
                            adresine bildirin. Sorumlu açıklama kapsamında raporları değerlendiriyoruz.
                        </p>
                    </section>
                </div>
            </div>
        </section>
    );
};

export default SecurityPage;
