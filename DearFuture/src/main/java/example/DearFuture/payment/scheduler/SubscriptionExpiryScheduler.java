package example.DearFuture.payment.scheduler;

import example.DearFuture.mail.*;
import example.DearFuture.payment.service.SubscriptionPaymentService;
import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.SubscriptionPlanRepository;
import example.DearFuture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Her gün çalışan scheduler:
 * 1. Süresi dolmuş abonelikleri bulur ve saklanan kart ile otomatik ödeme almaya çalışır.
 * 2. Ödeme başarısız olursa kullanıcıya bildirim e-postası gönderir.
 * 3. 7 gün ardışık başarısız ödeme sonrası aboneliği iptal eder ve iptal e-postası gönderir.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiryScheduler {

    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPaymentService subscriptionPaymentService;
    private final MailService mailService;

    private static final int MAX_RETRY_DAYS = 7;

    /**
     * Her gün 03:00'te çalışır:
     * - subscriptionEndsAt geçmiş ve plan FREE olmayan kullanıcıları bulur
     * - Saklanan kart ile ödeme almaya çalışır
     * - 7 gün ardışık başarısızlık sonrası aboneliği iptal eder
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void processSubscriptionRenewals() {
        LocalDateTime now = LocalDateTime.now();

        List<User> usersNeedingRenewal = userRepository.findExpiredPaidSubscriptions(now, "FREE");

        if (usersNeedingRenewal.isEmpty()) {
            log.debug("No subscriptions need renewal at {}", now);
            return;
        }

        log.info("Found {} subscriptions needing renewal", usersNeedingRenewal.size());

        for (User user : usersNeedingRenewal) {
            try {
                processUserRenewal(user);
            } catch (Exception e) {
                log.error("Error processing renewal for userId={}: {}", user.getId(), e.getMessage(), e);
            }
        }
    }

    private void processUserRenewal(User user) {
        // 7 gün veya daha fazla ardışık başarısız ödeme varsa aboneliği iptal et
        if (user.getConsecutivePaymentFailures() >= MAX_RETRY_DAYS) {
            cancelSubscriptionDueToPaymentFailure(user);
            return;
        }

        // Ödeme almayı dene
        boolean success = subscriptionPaymentService.processRecurringPayment(user);

        if (!success) {
            // processRecurringPayment zaten sayacı artırdı ve hata mailini gönderdi
            User freshUser = userRepository.findById(user.getId()).orElse(user);
            if (freshUser.getConsecutivePaymentFailures() >= MAX_RETRY_DAYS) {
                cancelSubscriptionDueToPaymentFailure(freshUser);
            }
        }
    }

    /**
     * 7 gün ardışık başarısız ödeme sonrası aboneliği iptal eder ve iptal e-postası gönderir.
     */
    private void cancelSubscriptionDueToPaymentFailure(User user) {
        String planName = user.getSubscriptionPlan() != null ? user.getSubscriptionPlan().getName() : "Bilinmeyen";

        log.info("Cancelling subscription due to {} consecutive payment failures: userId={}, plan={}",
                user.getConsecutivePaymentFailures(), user.getId(), planName);

        SubscriptionPlan freePlan = planRepository.findByCode("FREE").orElse(null);
        user.setSubscriptionPlan(freePlan);
        user.setSubscriptionEndsAt(null);
        user.setConsecutivePaymentFailures(0);
        user.setPaymentFailedSince(null);
        userRepository.save(user);

        // İptal e-postası gönder
        String htmlBody = SubscriptionCancelledEmailTemplate.build(planName);
        EmailMessage emailMessage = EmailMessage.builder()
                .toEmail(user.getEmail())
                .subject("Dear Future - Abonelik İptal Edildi")
                .body(htmlBody)
                .isHtml(true)
                .build();
        mailService.enqueueEmail(emailMessage);

        log.info("Subscription cancelled and email sent: userId={}, previousPlan={}", user.getId(), planName);
    }
}
