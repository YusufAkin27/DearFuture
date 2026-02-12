package example.DearFuture.admin.config;

import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        updateExistingPlanDefaults();
        try {
            planMigrationHelper.migrateExistingUsersToPlanEntity();
        } catch (Exception e) {
            log.debug("User plan migration skipped or failed: {}", e.getMessage());
        }
    }

    private void createDefaultPlans() {
        if (planRepository.count() > 0) {
            log.info("Subscription plans already exist, applying default limits.");
            return;
        }

        SubscriptionPlan free = SubscriptionPlan.builder()
                .code("FREE")
                .name("Ücretsiz")
                .description("3 mesaj hakkı (toplam), her ay yenilenmez")
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
                .features(new ArrayList<>(List.of("3 mesaj hakkı (toplam)", "Her ay yenilenmez", "Fotoğraf/dosya yüklenemez", "Sadece metin", "1 alıcı / mesaj")))
                .recommended(false)
                .active(true)
                .displayOrder(0)
                .build();

        SubscriptionPlan plus = SubscriptionPlan.builder()
                .code("PLUS")
                .name("Plus")
                .description("15 mesaj/ay, her ay yenilenir")
                .monthlyPrice(BigDecimal.valueOf(100))
                .priceLabel("₺/ay")
                .maxMessages(15)
                .maxRecipientsPerMessage(5)
                .allowPhoto(true)
                .allowFile(true)
                .allowVoice(false)
                .maxPhotosPerMessage(2)
                .maxPhotoSizeBytes(5L * 1024 * 1024)
                .maxFilesPerMessage(2)
                .maxFileSizeBytes(10L * 1024 * 1024)
                .features(new ArrayList<>(List.of("15 mesaj/ay", "Her ay yenilenir", "Fotoğraf & dosya", "5 alıcı / mesaj", "Öncelikli özellikler")))
                .recommended(true)
                .active(true)
                .displayOrder(1)
                .build();

        SubscriptionPlan premium = SubscriptionPlan.builder()
                .code("PREMIUM")
                .name("Premium")
                .description("20 mesaj/ay, her ay yenilenir")
                .monthlyPrice(BigDecimal.valueOf(150))
                .priceLabel("₺/ay")
                .maxMessages(20)
                .maxRecipientsPerMessage(20)
                .allowPhoto(true)
                .allowFile(true)
                .allowVoice(true)
                .maxPhotosPerMessage(5)
                .maxPhotoSizeBytes(10L * 1024 * 1024)
                .maxFilesPerMessage(5)
                .maxFileSizeBytes(20L * 1024 * 1024)
                .features(new ArrayList<>(List.of("20 mesaj/ay", "Her ay yenilenir", "Fotoğraf, dosya & ses kaydı", "20 alıcı / mesaj", "Tüm özellikler")))
                .recommended(false)
                .active(true)
                .displayOrder(2)
                .build();

        planRepository.saveAll(List.of(free, plus, premium));
        log.info("Default subscription plans created: FREE (3), PLUS (15), PREMIUM (20)");
    }

    /** Mevcut planları kod ile bulup limit ve özellikleri günceller (maxMessages, allowPhoto, allowFile vb.). */
    private void updateExistingPlanDefaults() {
        planRepository.findByCode("FREE").ifPresent(plan -> {
            plan.setMaxMessages(3);
            plan.setAllowPhoto(false);
            plan.setAllowFile(false);
            plan.setAllowVoice(false);
            plan.setMaxPhotosPerMessage(0);
            plan.setMaxPhotoSizeBytes(0L);
            plan.setMaxFilesPerMessage(0);
            plan.setMaxFileSizeBytes(0L);
            plan.setFeatures(new ArrayList<>(List.of("3 mesaj hakkı (toplam)", "Her ay yenilenmez", "Fotoğraf/dosya yüklenemez", "Sadece metin", "1 alıcı / mesaj")));
            planRepository.save(plan);
            log.info("Updated plan FREE to default limits");
        });
        planRepository.findByCode("PLUS").ifPresent(plan -> {
            plan.setMaxMessages(15);
            plan.setAllowPhoto(true);
            plan.setAllowFile(true);
            plan.setAllowVoice(false);
            plan.setMaxPhotosPerMessage(2);
            plan.setMaxPhotoSizeBytes(5L * 1024 * 1024);
            plan.setMaxFilesPerMessage(2);
            plan.setMaxFileSizeBytes(10L * 1024 * 1024);
            plan.setFeatures(new ArrayList<>(List.of("15 mesaj/ay", "Her ay yenilenir", "Fotoğraf & dosya", "5 alıcı / mesaj", "Öncelikli özellikler")));
            planRepository.save(plan);
            log.info("Updated plan PLUS to default limits");
        });
        planRepository.findByCode("PREMIUM").ifPresent(plan -> {
            plan.setMaxMessages(20);
            plan.setAllowPhoto(true);
            plan.setAllowFile(true);
            plan.setAllowVoice(true);
            plan.setMaxPhotosPerMessage(5);
            plan.setMaxPhotoSizeBytes(10L * 1024 * 1024);
            plan.setMaxFilesPerMessage(5);
            plan.setMaxFileSizeBytes(20L * 1024 * 1024);
            plan.setFeatures(new ArrayList<>(List.of("20 mesaj/ay", "Her ay yenilenir", "Fotoğraf, dosya & ses kaydı", "20 alıcı / mesaj", "Tüm özellikler")));
            planRepository.save(plan);
            log.info("Updated plan PREMIUM to default limits");
        });
    }
}
