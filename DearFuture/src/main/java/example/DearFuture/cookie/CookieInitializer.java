package example.DearFuture.cookie;

import example.DearFuture.contract.Contract;
import example.DearFuture.contract.ContractRepository;
import example.DearFuture.contract.ContractType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Uygulama başlangıcında çerez politikası sözleşmesinin var olduğundan emin olur.
 * ContractInitializer tüm sözleşmeleri (CEREZ dahil) oluşturur; bu initializer
 * yalnızca CEREZ (Çerez Politikası) eksikse oluşturur (yedek / çerez modülü init).
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class CookieInitializer implements CommandLineRunner {

    private final ContractRepository contractRepository;

    @Override
    public void run(String... args) {
        if (contractRepository.existsByType(ContractType.CEREZ)) {
            log.info("Çerez politikası sözleşmesi zaten mevcut, atlanıyor.");
            return;
        }

        log.info("Çerez politikası sözleşmesi oluşturuluyor...");
        createCookiePolicyContract();
        log.info("Çerez politikası sözleşmesi oluşturuldu.");
    }

    private void createCookiePolicyContract() {
        Contract contract = Contract.builder()
                .type(ContractType.CEREZ)
                .title("Çerez Politikası")
                .content(getCookiePolicyContent())
                .version(1)
                .active(true)
                .requiredApproval(true)
                .build();
        contractRepository.save(contract);
    }

    private String getCookiePolicyContent() {
        return """
                <h2>ÇEREZ POLİTİKASI</h2>
                
                <h3>1. ÇEREZLER HAKKINDA</h3>
                <p>Web sitemiz, kullanıcı deneyimini iyileştirmek ve site işlevselliğini sağlamak amacıyla çerezler kullanmaktadır. Bu politika, web sitemizde kullanılan çerezler ve çerez tercihleriniz hakkında bilgilendirme amacıyla hazırlanmıştır.</p>
                
                <h3>2. ÇEREZ NEDİR?</h3>
                <p>Çerezler, bir web sitesini ziyaret ettiğinizde tarayıcınız tarafından cihazınıza kaydedilen küçük metin dosyalarıdır. Çerezler, web sitesinin daha iyi çalışmasını sağlar ve kullanıcı deneyimini iyileştirir.</p>
                
                <h3>3. ÇEREZ TÜRLERİ</h3>
                <h4>3.1. Zorunlu Çerezler</h4>
                <p>Bu çerezler, web sitesinin temel işlevlerinin çalışması için gereklidir. Site güvenliği, oturum yönetimi ve temel işlevsellik için kullanılır. Bu çerezler olmadan web sitesi düzgün çalışmaz.</p>
                
                <h4>3.2. Analitik Çerezler</h4>
                <p>Bu çerezler, web sitesinin nasıl kullanıldığını anlamamıza yardımcı olur. Ziyaretçi sayısı, sayfa görüntüleme sayıları ve kullanıcı davranışları hakkında bilgi toplar.</p>
                
                <h4>3.3. Pazarlama Çerezleri</h4>
                <p>Bu çerezler, size daha uygun reklamlar ve içerikler sunmak için kullanılır.</p>
                
                <h3>4. ÇEREZ YÖNETİMİ</h3>
                <p>Tarayıcı ayarlarınızdan çerezleri yönetebilir, silebilir veya engelleyebilirsiniz. Zorunlu çerezleri engellemeniz durumunda web sitesinin bazı özellikleri çalışmayabilir.</p>
                
                <h3>5. GÜNCELLEMELER</h3>
                <p>Bu çerez politikası, gerektiğinde güncellenebilir. Önemli değişiklikler durumunda size bildirim yapılacaktır.</p>
                
                <h3>6. İLETİŞİM</h3>
                <p>Çerezler hakkında sorularınız için bizimle iletişime geçebilirsiniz.</p>
                """;
    }
}
