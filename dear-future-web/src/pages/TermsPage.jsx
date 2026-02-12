import './LegalPage.css';

const TermsPage = () => {
    return (
        <section className="legal-container">
            <div className="legal-inner">
                <header className="legal-hero">
                    <span className="legal-pill">Yasal</span>
                    <h1>Kullanım Şartları</h1>
                    <p className="legal-updated">Son güncelleme: 1 Şubat 2026</p>
                </header>

                <div className="legal-content">
                    <section>
                        <h2>1. Şartların Kabulü</h2>
                        <p>
                            Dear Future hizmetine erişerek veya kullanarak bu Kullanım Şartları ile bağlı
                            kalmayı kabul etmiş olursunuz.
                        </p>
                    </section>

                    <section>
                        <h2>2. Hizmetin Kullanımı</h2>
                        <p>
                            Dear Future'ı yalnızca yasal amaçlarla ve bu Şartlara uygun biçimde kullanmayı
                            kabul edersiniz. Hesap bilgilerinizin gizliliğinden siz sorumlusunuz. Hesabınızı
                            yetkisiz kullanıma karşı korumak sizin yükümlülüğünüzdür.
                        </p>
                    </section>

                    <section>
                        <h2>3. Kullanıcı İçeriği</h2>
                        <p>
                            Oluşturduğunuz içeriğin mülkiyeti size aittir. Bununla birlikte, talimatlarınız
                            doğrultusunda içeriğinizi saklamak ve iletmek için bize lisans verirsiniz.
                        </p>
                    </section>

                    <section>
                        <h2>4. Hesabın Sona Ermesi</h2>
                        <p>
                            Bu Şartları ihlal etmeniz halinde hesabınızı askıya alma veya sonlandırma hakkımız
                            saklıdır. Hesabınızı istediğiniz zaman kapatabilirsiniz.
                        </p>
                    </section>

                    <section>
                        <h2>5. Sorumluluk Reddi</h2>
                        <p>
                            Hizmet "olduğu gibi" sunulmaktadır; açık veya zımni hiçbir garanti verilmemektedir.
                            Yasal haklarınız saklı kalmak kaydıyla, hizmetten doğan zararlardan sorumluluk
                            sınırlıdır.
                        </p>
                    </section>
                </div>
            </div>
        </section>
    );
};

export default TermsPage;
