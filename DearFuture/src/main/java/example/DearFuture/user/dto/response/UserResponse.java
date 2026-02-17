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
    /** Kullanıcının abonelik plan kodu */
    private String subscriptionPlanCode;
    /** Kullanıcının abonelik plan adı */
    private String subscriptionPlanName;
    private LocalDateTime subscriptionEndsAt;
    /** Etkin plana göre maksimum mesaj sayısı */
    private int maxMessagesPerPlan;
    /** Mesaj başına max fotoğraf sayısı */
    private Integer maxPhotosPerMessage;
    /** Fotoğraf başına max boyut (byte) */
    private Long maxPhotoSizeBytes;
    /** Mesaj başına max dosya sayısı */
    private Integer maxFilesPerMessage;
    /** Dosya başına max boyut (byte) */
    private Long maxFileSizeBytes;
    /** Mesaj başına max alıcı sayısı */
    private Integer maxRecipientsPerMessage;
    /** Ses kaydı ekleme izni */
    private Boolean allowVoice;
    /** Mesaj başına max ses kaydı sayısı */
    private Integer maxAudioPerMessage;
    /** Ses kaydı başına max boyut (byte) */
    private Long maxAudioSizeBytes;

    private String locale;
    private boolean emailNotifications;
    private boolean marketingEmails;

    public static UserResponse fromUser(User user) {
        SubscriptionPlan plan = user.getSubscriptionPlan();
        boolean expired = user.getSubscriptionEndsAt() != null && LocalDateTime.now().isAfter(user.getSubscriptionEndsAt());
        // Plan null veya süresi dolmuşsa, plan bilgilerini default (FREE) gibi göster
        SubscriptionPlan effective = (plan == null || (expired && !plan.isFree())) ? null : plan;

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
                .subscriptionPlanCode(effective != null ? effective.getCode() : "FREE")
                .subscriptionPlanName(effective != null ? effective.getName() : "Ücretsiz")
                .subscriptionEndsAt(user.getSubscriptionEndsAt())
                .maxMessagesPerPlan(effective != null ? effective.getMaxMessages() : 3)
                .maxPhotosPerMessage(effective != null && effective.getMaxPhotosPerMessage() > 0 ? effective.getMaxPhotosPerMessage() : null)
                .maxPhotoSizeBytes(effective != null && effective.getMaxPhotoSizeBytes() > 0 ? effective.getMaxPhotoSizeBytes() : null)
                .maxFilesPerMessage(effective != null && effective.getMaxFilesPerMessage() > 0 ? effective.getMaxFilesPerMessage() : null)
                .maxFileSizeBytes(effective != null && effective.getMaxFileSizeBytes() > 0 ? effective.getMaxFileSizeBytes() : null)
                .maxRecipientsPerMessage(effective != null && effective.getMaxRecipientsPerMessage() > 0 ? effective.getMaxRecipientsPerMessage() : 1)
                .allowVoice(effective != null && effective.isAllowVoice())
                .maxAudioPerMessage(effective != null && effective.getMaxAudioPerMessage() > 0 ? effective.getMaxAudioPerMessage() : null)
                .maxAudioSizeBytes(effective != null && effective.getMaxAudioSizeBytes() > 0 ? effective.getMaxAudioSizeBytes() : null)
                .locale(user.getLocale() != null ? user.getLocale() : "tr")
                .emailNotifications(user.isEmailNotifications())
                .marketingEmails(user.isMarketingEmails())
                .build();
    }
}
