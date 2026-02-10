package example.DearFuture.admin.config;

import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Uygulama başlangıcında varsayılan abonelik planlarını oluşturur.
 * Planlar zaten varsa tekrar oluşturmaz.
 * Mevcut kullanıcıların eski enum tabanlı plan verilerini yeni entity referansına taşır.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanDataInitializer implements CommandLineRunner {

    private final SubscriptionPlanRepository planRepository;
    private final PlanMigrationHelper planMigrationHelper;

    @Override
    @Transactional
    public void run(String... args) {
        createDefaultPlans();
        try {
            planMigrationHelper.migrateExistingUsersToPlanEntity();
        } catch (Exception e) {
            log.debug("User plan migration skipped or failed: {}", e.getMessage());
        }
    }

    private void createDefaultPlans() {
        if (planRepository.count() > 0) {
            log.info("Subscription plans already exist, skipping initialization.");
            return;
        }

        SubscriptionPlan free = SubscriptionPlan.builder()
                .code("FREE")
                .name("Ücretsiz")
                .description("Temel özelliklerle başlayın")
                .monthlyPrice(BigDecimal.ZERO)
                .priceLabel("₺/ay")
                .maxMessages(3)
                .maxRecipientsPerMessage(1)
                .allowPhoto(false)
                .allowFile(false)
                .allowVoice(false)
                .maxPhotosPerMessage(0)
                .maxPhotoSizeBytes(0L)
                .maxFilesPerMessage(0)
                .maxFileSizeBytes(0L)
                .features(List.of("3 zamanlanmış mesaj", "Sadece metin", "1 alıcı / mesaj"))
                .recommended(false)
                .active(true)
                .displayOrder(0)
                .build();

        SubscriptionPlan plus = SubscriptionPlan.builder()
                .code("PLUS")
                .name("Plus")
                .description("Daha fazla mesaj ve medya desteği")
                .monthlyPrice(BigDecimal.valueOf(100))
                .priceLabel("₺/ay")
                .maxMessages(20)
                .maxRecipientsPerMessage(5)
                .allowPhoto(true)
                .allowFile(true)
                .allowVoice(false)
                .maxPhotosPerMessage(2)
                .maxPhotoSizeBytes(5L * 1024 * 1024)
                .maxFilesPerMessage(2)
                .maxFileSizeBytes(10L * 1024 * 1024)
                .features(List.of("20 zamanlanmış mesaj", "Fotoğraf & dosya", "5 alıcı / mesaj", "Öncelikli özellikler"))
                .recommended(true)
                .active(true)
                .displayOrder(1)
                .build();

        SubscriptionPlan premium = SubscriptionPlan.builder()
                .code("PREMIUM")
                .name("Premium")
                .description("Tüm özellikler, en yüksek limitler")
                .monthlyPrice(BigDecimal.valueOf(150))
                .priceLabel("₺/ay")
                .maxMessages(100)
                .maxRecipientsPerMessage(20)
                .allowPhoto(true)
                .allowFile(true)
                .allowVoice(true)
                .maxPhotosPerMessage(5)
                .maxPhotoSizeBytes(10L * 1024 * 1024)
                .maxFilesPerMessage(5)
                .maxFileSizeBytes(20L * 1024 * 1024)
                .features(List.of("100 zamanlanmış mesaj", "Fotoğraf, dosya & ses kaydı", "20 alıcı / mesaj", "Tüm özellikler"))
                .recommended(false)
                .active(true)
                .displayOrder(2)
                .build();

        planRepository.saveAll(List.of(free, plus, premium));
        log.info("Default subscription plans created: FREE, PLUS, PREMIUM");
    }
}
