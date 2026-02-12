package example.DearFuture.user.dto.response;

import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private boolean emailVerified;
    /** Kullanıcının abonelik plan kodu: FREE, PLUS, PREMIUM */
    private String subscriptionPlanCode;
    /** Kullanıcının abonelik plan adı */
    private String subscriptionPlanName;
    /** Aboneliğin bitiş tarihi; null ise süresiz veya FREE */
    private LocalDateTime subscriptionEndsAt;
    /** Planın maksimum mesaj limiti */
    private int maxMessagesPerPlan;

    public static ProfileResponse fromUser(User user) {
        SubscriptionPlan plan = user.getSubscriptionPlan();
        boolean expired = user.getSubscriptionEndsAt() != null && LocalDateTime.now().isAfter(user.getSubscriptionEndsAt());
        SubscriptionPlan effective = (plan == null || (expired && !plan.isFree())) ? null : plan;
        return ProfileResponse.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .emailVerified(user.isEmailVerified())
                .subscriptionPlanCode(effective != null ? effective.getCode() : "FREE")
                .subscriptionPlanName(effective != null ? effective.getName() : "Ücretsiz")
                .subscriptionEndsAt(user.getSubscriptionEndsAt())
                .maxMessagesPerPlan(effective != null ? effective.getMaxMessages() : 3)
                .build();
    }
}
