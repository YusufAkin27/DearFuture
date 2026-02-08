package example.DearFuture.payment.scheduler;

import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Üyelik süresi dolduktan 7 gün sonra hâlâ yenileme yoksa kullanıcıyı FREE plana düşürür.
 * (Aylık otomatik tahsilat için iyzico Subscription API entegrasyonu gerekir; bu job sadece süre + 7 gün kuralını uygular.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiryScheduler {

    private final UserRepository userRepository;

    /** Her gün 02:00'da çalışır: subscriptionEndsAt + 7 gün geçmiş kullanıcıları FREE yapar */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void expireSubscriptionsAfterGracePeriod() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime graceEnd = now.minusDays(7);
        List<User> expired = userRepository.findBySubscriptionEndsAtBeforeAndSubscriptionPlanIn(
                graceEnd, List.of(SubscriptionPlan.PLUS, SubscriptionPlan.PREMIUM));
        for (User user : expired) {
            user.setSubscriptionPlan(SubscriptionPlan.FREE);
            user.setSubscriptionEndsAt(null);
            userRepository.save(user);
            log.info("Subscription expired (grace period passed), set to FREE: userId={}", user.getId());
        }
    }
}
