package example.DearFuture.user.dto.response;

import example.DearFuture.user.entity.Role;
import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private Set<Role> roles;
    private boolean enabled;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    /** Kullanıcının abonelik planı */
    private SubscriptionPlan subscriptionPlan;
    private LocalDateTime subscriptionEndsAt;
    /** Etkin plana göre maksimum mesaj sayısı */
    private int maxMessagesPerPlan;
    /** Mesaj başına max fotoğraf sayısı (PLUS/PREMIUM) */
    private Integer maxPhotosPerMessage;
    /** Fotoğraf başına max boyut (byte) */
    private Long maxPhotoSizeBytes;
    /** Mesaj başına max dosya sayısı */
    private Integer maxFilesPerMessage;
    /** Dosya başına max boyut (byte) */
    private Long maxFileSizeBytes;

    private String locale;
    private boolean emailNotifications;
    private boolean marketingEmails;

    public static UserResponse fromUser(User user) {
        SubscriptionPlan plan = user.getSubscriptionPlan() != null ? user.getSubscriptionPlan() : SubscriptionPlan.FREE;
        boolean expired = user.getSubscriptionEndsAt() != null && LocalDateTime.now().isAfter(user.getSubscriptionEndsAt());
        SubscriptionPlan effective = expired ? SubscriptionPlan.FREE : plan;
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .subscriptionPlan(user.getSubscriptionPlan())
                .subscriptionEndsAt(user.getSubscriptionEndsAt())
                .maxMessagesPerPlan(effective.getMaxMessages())
                .maxPhotosPerMessage(effective.getMaxPhotosPerMessage() > 0 ? effective.getMaxPhotosPerMessage() : null)
                .maxPhotoSizeBytes(effective.getMaxPhotoSizeBytes() > 0 ? effective.getMaxPhotoSizeBytes() : null)
                .maxFilesPerMessage(effective.getMaxFilesPerMessage() > 0 ? effective.getMaxFilesPerMessage() : null)
                .maxFileSizeBytes(effective.getMaxFileSizeBytes() > 0 ? effective.getMaxFileSizeBytes() : null)
                .locale(user.getLocale() != null ? user.getLocale() : "tr")
                .emailNotifications(user.isEmailNotifications())
                .marketingEmails(user.isMarketingEmails())
                .build();
    }
}
