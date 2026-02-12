import './LegalPage.css';

const PrivacyPage = () => {
    return (
        <section className="legal-container">
            <div className="legal-inner">
                <header className="legal-hero">
                    <span className="legal-pill">Gizlilik</span>
                    <h1>Gizlilik Politikası</h1>
                    <p className="legal-updated">Son güncelleme: 1 Şubat 2026</p>
                </header>

                <div className="legal-content">
                    <section>
                        <h2>1. Giriş</h2>
                        <p>
                            Dear Future olarak gizliliğinize en üst düzeyde önem veriyoruz. Bu Gizlilik Politikası,
                            bilgilerinizi nasıl topladığımızı, kullandığımızı ve koruduğumuzu açıklamaktadır.
                        </p>
                    </section>

                    <section>
                        <h2>2. Veri Toplama</h2>
                        <p>
                            Hizmetlerimizi sunmak için gerekli asgari verileri topluyoruz; örneğin hesap oluşturma
                            ve bildirimler için e-posta adresiniz. Yalnızca hizmet kalitesi ve güvenliği için
                            ihtiyaç duyduğumuz bilgileri işliyoruz.
                        </p>
                    </section>

                    <section>
                        <h2>3. Uçtan Uca Şifreleme</h2>
                        <p>
                            Mesajlarınız (zaman kapsülleri), sunucularımıza ulaşmadan önce cihazınızda şifrelenir.
                            Mesajlarınızın şifresini çözmek için anahtarlara sahip değiliz. Yalnızca belirlediğiniz
                            alıcı mesajları okuyabilir.
                        </p>
                    </section>

                    <section>
                        <h2>4. Veri Saklama</h2>
                        <p>
                            Verilerinizi yalnızca bu politikada belirtilen amaçları yerine getirmek için veya
                            yasaların gerektirdiği süre boyunca saklarız. Hesabınızı sildiğinizde ilgili veriler
                            silinir veya anonim hale getirilir.
                        </p>
                    </section>

                    <section>
                        <h2>5. İletişim</h2>
                        <p>
                            Gizlilik Politikamız hakkında sorularınız için bize gizlilik@dearfuture.com adresinden
                            ulaşabilirsiniz.
                        </p>
                    </section>
                </div>
            </div>
        </section>
    );
};

export default PrivacyPage;
