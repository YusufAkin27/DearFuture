package example.DearFuture.user.service;

import example.DearFuture.exception.security.UserNotFoundException;
import example.DearFuture.message.entity.MessageStatus;
import example.DearFuture.message.repository.FutureMessageRepository;
import example.DearFuture.payment.entity.SubscriptionPayment;
import example.DearFuture.payment.repository.SubscriptionPaymentRepository;
import example.DearFuture.user.dto.response.UsageResponse;
import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.SubscriptionPlanRepository;
import example.DearFuture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {

    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final FutureMessageRepository futureMessageRepository;
    private final SubscriptionPaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public UsageResponse getUsage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));
        SubscriptionPlan plan = effectivePlan(user);

        if (plan == null || plan.isFree()) {
            return buildFreeUsage(user);
        }
        return buildPaidUsage(user, plan);
    }

    private UsageResponse buildFreeUsage(User user) {
        SubscriptionPlan freePlan = planRepository.findByCode("FREE").orElse(null);
        long limit = freePlan != null ? freePlan.getMaxMessages() : 3L;
        long used = futureMessageRepository.countByUserAndStatusIn(user,
                Set.of(MessageStatus.SCHEDULED, MessageStatus.QUEUED, MessageStatus.SENT));
        long remaining = Math.max(0L, limit - used);

        return UsageResponse.builder()
                .planCode(freePlan != null ? freePlan.getCode() : "FREE")
                .planName(freePlan != null ? freePlan.getName() : "Ücretsiz")
                .limit(limit)
                .used(used)
                .remaining(remaining)
                .resetsMonthly(false)
                .periodStart(null)
                .periodEnd(null)
                .build();
    }

    private UsageResponse buildPaidUsage(User user, SubscriptionPlan plan) {
        long limit = plan.getMaxMessages();
        Instant periodStart;
        Instant periodEnd;

        var lastPayment = paymentRepository.findTopByUserIdAndStatusOrderByPaidAtDesc(user.getId(), SubscriptionPayment.PaymentStatus.SUCCESS);

        if (lastPayment.isPresent() && lastPayment.get().getPeriodEnd() != null && lastPayment.get().getPeriodStart() != null
                && LocalDateTime.now().isBefore(lastPayment.get().getPeriodEnd())) {
            periodStart = toInstant(lastPayment.get().getPeriodStart());
            periodEnd = toInstant(lastPayment.get().getPeriodEnd());
        } else {
            LocalDateTime endLdt = user.getSubscriptionEndsAt() != null ? user.getSubscriptionEndsAt() : LocalDateTime.now().plusMonths(1);
            LocalDateTime startLdt = endLdt.minusMonths(1);
            periodStart = toInstant(startLdt);
            periodEnd = toInstant(endLdt);
        }

        long used = futureMessageRepository.countByUserIdAndScheduledAtBetween(user.getId(), periodStart, periodEnd);
        long remaining = Math.max(0L, limit - used);

        return UsageResponse.builder()
                .planCode(plan.getCode())
                .planName(plan.getName())
                .limit(limit)
                .used(used)
                .remaining(remaining)
                .resetsMonthly(true)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .build();
    }

    private Instant toInstant(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }

    private SubscriptionPlan effectivePlan(User user) {
        if (user.getSubscriptionEndsAt() != null && LocalDateTime.now().isAfter(user.getSubscriptionEndsAt())) {
            return planRepository.findByCode("FREE").orElse(null);
        }
        return user.getSubscriptionPlan() != null ? user.getSubscriptionPlan() : planRepository.findByCode("FREE").orElse(null);
    }
}
