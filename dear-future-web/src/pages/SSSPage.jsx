import {
    Accordion,
    AccordionItem,
    AccordionTrigger,
    AccordionPanel,
} from '../components/Accordion';
import './SSSPage.css';

const SSS_CATEGORIES = [
    {
        category: 'Genel',
        items: [
            {
                title: 'Dear Future nedir?',
                content:
                    'Dear Future, kendinize veya sevdiklerinize gelecekte ulaşacak dijital mektuplar, fotoğraflar ve videolar göndermenizi sağlayan bir zaman kapsülü platformudur. Bugün yazdığınız bir mesajı bir hafta, bir ay, bir yıl hatta on yıl sonrasına planlayabilirsiniz. Mesajlarınız güvenli sunucularda şifreli olarak saklanır ve yalnızca belirlediğiniz tarihte alıcıya ulaştırılır. Amacımız, insanların anılarını ve duygularını zamana karşı koruyabilmesini sağlamaktır.',
            },
            {
                title: 'Dear Future nasıl çalışır?',
                content:
                    'Çalışma mantığı oldukça basittir:\n\n1. Hesap Oluşturma: Google hesabınızla veya e-posta adresinizle hızlıca kayıt olun.\n2. Mesaj Yazma: "Yeni Mesaj" sayfasından mesajınızı yazın; isterseniz fotoğraf veya video da ekleyebilirsiniz.\n3. Tarih Seçimi: Mesajınızın ne zaman teslim edileceğini belirleyin — yarın, gelecek ay veya yıllar sonrası.\n4. Gönderim: Mesajınız sunucularımızda şifrelenerek saklanır. Belirlediğiniz tarih geldiğinde sistemimiz otomatik olarak alıcıya e-posta gönderir.\n5. Mesajı Görüntüleme: Alıcı e-postadaki özel bağlantıya tıklayarak mesajınızı güvenli bir şekilde görüntüler.\n\nTüm bu süreç tamamen otomatiktir; mesajınız saatine kadar doğru şekilde teslim edilir.',
            },
            {
                title: 'Kimler kullanabilir?',
                content:
                    'Dear Future herkese açıktır. Kendinize gelecekte bir hatırlatma bırakmak isteyen bireylerden, çocuklarına yıllar sonra ulaşacak mektuplar yazmak isteyen ebeveynlere, öğrencilerine mezuniyet sürprizi hazırlamak isteyen öğretmenlere kadar herkes platformumuzu kullanabilir. Yaş sınırı veya mesleki kısıtlama yoktur.',
            },
        ],
    },
    {
        category: 'Hesap ve Giriş',
        items: [
            {
                title: 'Nasıl hesap oluşturabilirim?',
                content:
                    'Dear Future\'a Google hesabınızla tek tıkla giriş yapabilirsiniz. Alternatif olarak e-posta adresinizle kayıt olabilirsiniz; bu durumda size gönderilen doğrulama kodunu girerek hesabınızı aktif hale getirirsiniz. Kayıt işlemi birkaç saniye sürer ve herhangi bir kişisel bilgi (TC kimlik, telefon vb.) istenmez.',
            },
            {
                title: 'Şifremi unuttum, ne yapmalıyım?',
                content:
                    'Google ile giriş yapıyorsanız şifre yönetimi Google tarafından sağlanır. E-posta ile kayıt olduysanız, giriş sayfasında her seferinde e-postanıza gönderilen tek kullanımlık doğrulama kodu ile giriş yaparsınız; bu nedenle ayrı bir şifre hatırlama zorunluluğunuz yoktur.',
            },
            {
                title: 'Hesabımı silebilir miyim?',
                content:
                    'Evet. Ayarlar sayfasından hesabınızı kalıcı olarak silebilirsiniz. Hesabınız silindiğinde tüm mesajlarınız, ekleriniz ve kişisel verileriniz sunucularımızdan geri dönüşü olmayacak şekilde kaldırılır. Henüz teslim edilmemiş mesajlarınız da iptal edilir.',
            },
        ],
    },
    {
        category: 'Mesajlar',
        items: [
            {
                title: 'Mesajıma fotoğraf veya video ekleyebilir miyim?',
                content:
                    'Evet. Mesaj oluştururken fotoğraf ve video dosyaları ekleyebilirsiniz. Ücretsiz planda belirli bir dosya boyutu sınırı uygulanır. Daha büyük dosyalar veya daha fazla ek için ücretli planlarımızdan birine geçiş yapabilirsiniz. Desteklenen formatlar arasında JPEG, PNG, GIF, MP4 ve WEBM bulunur.',
            },
            {
                title: 'Gönderdiğim mesajı düzenleyebilir veya silebilir miyim?',
                content:
                    'Mesajınız henüz teslim edilmediyse profil sayfanızdan mesajlarınızı görüntüleyebilir, düzenleyebilir veya silebilirsiniz. Ancak teslim tarihi gelip mesaj alıcıya ulaştırıldıktan sonra içerik değiştirilemez. Bu nedenle mesajınızı göndermeden önce dikkatle kontrol etmenizi öneririz.',
            },
            {
                title: 'Mesajımı birden fazla kişiye gönderebilir miyim?',
                content:
                    'Şu anda her mesaj tek bir alıcıya gönderilebilir. Birden fazla kişiye ulaşmak istiyorsanız her biri için ayrı mesaj oluşturabilirsiniz. İleride toplu gönderim özelliği de eklemeyi planlıyoruz.',
            },
            {
                title: 'Mesajımı "Herkese Açık" yaparsam ne olur?',
                content:
                    'Herkese açık olarak işaretlediğiniz mesajlar, "Herkese Açık Mesajlar" sayfasında anonim veya isminizle birlikte görüntülenir. Bu sayede ilham verici, motive edici veya duygusal mesajlarınızı toplulukla paylaşabilirsiniz. Kişisel bilgileriniz paylaşılmaz; yalnızca mesaj içeriği ve gönderim tarihi gösterilir.',
            },
        ],
    },
    {
        category: 'Güvenlik ve Gizlilik',
        items: [
            {
                title: 'Mesajlarım güvende mi?',
                content:
                    'Kesinlikle evet. Mesajlarınız sunucularımızda şifreli olarak saklanır. Yalnızca belirlediğiniz tarihte alıcıya özel bir bağlantı ile ulaştırılır. Dear Future ekibi dahil hiç kimse mesaj içeriğinize erişemez. SSL/TLS şifreleme ile veri aktarımı korunur, sunucu tarafında ise endüstri standardı güvenlik önlemleri uygulanır.',
            },
            {
                title: 'Kişisel verilerimi nasıl kullanıyorsunuz?',
                content:
                    'Kişisel verileriniz yalnızca hesap yönetimi ve mesaj teslimi amacıyla kullanılır. Verilerinizi üçüncü taraflarla paylaşmaz, reklam amacıyla kullanmaz ve satmayız. Detaylı bilgi için Gizlilik Politikamızı inceleyebilirsiniz.',
            },
        ],
    },
    {
        category: 'Abonelik ve Fiyatlandırma',
        items: [
            {
                title: 'Ücretsiz plan neleri içerir?',
                content:
                    'Ücretsiz plan ile belirli sayıda mesaj gönderebilir, temel dosya ekleme özelliğini kullanabilir ve tüm platform özelliklerinden faydalanabilirsiniz. Ücretsiz plan günlük kullanım için yeterli bir kota sunar. Daha fazla mesaj kapasitesi, büyük dosya ekleri veya öncelikli destek için ücretli planlarımıza geçiş yapabilirsiniz.',
            },
            {
                title: 'Ücretli planlara nasıl geçiş yapabilirim?',
                content:
                    'Fiyatlandırma sayfasından size uygun planı seçip güvenli ödeme adımlarını tamamlayarak anında geçiş yapabilirsiniz. Aboneliğiniz otomatik olarak aktif hale gelir. İstediğiniz zaman Abonelik sayfasından planınızı yükseltebilir, düşürebilir veya iptal edebilirsiniz.',
            },
            {
                title: 'Aboneliğimi iptal edersem ne olur?',
                content:
                    'Aboneliğinizi iptal ettiğinizde mevcut dönem sonuna kadar ücretli plan özelliklerini kullanmaya devam edersiniz. Dönem sona erdiğinde hesabınız otomatik olarak ücretsiz plana döner. Daha önce gönderdiğiniz veya planladığınız mesajlar etkilenmez; hepsi zamanında teslim edilir.',
            },
        ],
    },
    {
        category: 'Teknik',
        items: [
            {
                title: 'Hangi cihazlardan kullanabilirim?',
                content:
                    'Dear Future tamamen web tabanlı bir platformdur ve modern bir tarayıcıya sahip her cihazdan erişilebilir. Masaüstü, dizüstü bilgisayar, tablet ve akıllı telefon — Chrome, Firefox, Safari, Edge gibi güncel tarayıcıların hepsinde sorunsuz çalışır. Herhangi bir uygulama indirmenize gerek yoktur.',
            },
            {
                title: 'Mesajım zamanında teslim edilmezse ne olur?',
                content:
                    'Sistemimiz mesajları dakika hassasiyetinde teslim edecek şekilde tasarlanmıştır. Sunucu bakımı veya beklenmedik bir teknik aksaklık durumunda mesajınız en kısa sürede teslim edilir. Ayrıca mesaj durumunuzu profil sayfanızdan her zaman takip edebilirsiniz. Herhangi bir sorunla karşılaşırsanız İletişim sayfamızdan bize ulaşabilirsiniz.',
            },
        ],
    },
];

const SSSPage = () => {
    let globalIdx = 0;
    return (
        <section className="sss-container">
            <div className="sss-inner">
                <header className="sss-header">
                    <span className="sss-pill">SSS</span>
                    <h1>Sıkça Sorulan Sorular</h1>
                    <p>Dear Future hakkında merak ettiğiniz her şeyin yanıtı burada.</p>
                </header>

                {SSS_CATEGORIES.map((cat) => (
                    <div key={cat.category} className="sss-category">
                        <h2 className="sss-category-title">{cat.category}</h2>
                        <Accordion className="sss-accordion">
                            {cat.items.map((item) => {
                                const val = `sss-${++globalIdx}`;
                                return (
                                    <AccordionItem key={val} value={val}>
                                        <AccordionTrigger showArrow>
                                            {item.title}
                                        </AccordionTrigger>
                                        <AccordionPanel>
                                            {item.content.split('\n').map((line, i) => (
                                                <p key={i}>{line}</p>
                                            ))}
                                        </AccordionPanel>
                                    </AccordionItem>
                                );
                            })}
                        </Accordion>
                    </div>
                ))}
            </div>
        </section>
    );
};

export default SSSPage;
