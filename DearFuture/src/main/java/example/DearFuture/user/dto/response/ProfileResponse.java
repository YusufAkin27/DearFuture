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
    /** Kullanıcının abonelik planı: FREE, PLUS, PREMIUM */
    private SubscriptionPlan subscriptionPlan;
    /** Aboneliğin bitiş tarihi; null ise süresiz veya FREE */
    private LocalDateTime subscriptionEndsAt;
    /** Planın maksimum mesaj limiti (frontend’de ilerleme göstermek için) */
    private int maxMessagesPerPlan;

    public static ProfileResponse fromUser(User user) {
        SubscriptionPlan plan = user.getSubscriptionPlan() != null ? user.getSubscriptionPlan() : SubscriptionPlan.FREE;
        boolean expired = user.getSubscriptionEndsAt() != null && LocalDateTime.now().isAfter(user.getSubscriptionEndsAt());
        SubscriptionPlan effective = expired ? SubscriptionPlan.FREE : plan;
        return ProfileResponse.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .emailVerified(user.isEmailVerified())
                .subscriptionPlan(user.getSubscriptionPlan())
                .subscriptionEndsAt(user.getSubscriptionEndsAt())
                .maxMessagesPerPlan(effective.getMaxMessages())
                .build();
    }
}
