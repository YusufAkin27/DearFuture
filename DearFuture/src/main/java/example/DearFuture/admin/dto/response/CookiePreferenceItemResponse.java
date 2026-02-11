package example.DearFuture.admin.dto.response;

import example.DearFuture.cookie.entity.CookiePreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CookiePreferenceItemResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private String sessionId;
    private String ipAddress;
    private Boolean necessary;
    private Boolean analytics;
    private Boolean marketing;
    private Boolean personalization;
    private Boolean consentGiven;
    private String consentVersion;
    private LocalDateTime consentDate;
    private LocalDateTime updatedAt;
    private LocalDateTime revokedAt;

    public static CookiePreferenceItemResponse fromEntity(CookiePreference cp) {
        String email = cp.getUser() != null ? cp.getUser().getEmail() : null;
        String name = null;
        if (cp.getUser() != null) {
            String f = cp.getUser().getFirstName();
            String l = cp.getUser().getLastName();
            if (f != null && !f.isBlank()) {
                name = f + (l != null && !l.isBlank() ? " " + l : "");
            }
        }
        return CookiePreferenceItemResponse.builder()
                .id(cp.getId())
                .userId(cp.getUser() != null ? cp.getUser().getId() : null)
                .userEmail(email)
                .userName(name)
                .sessionId(cp.getSessionId())
                .ipAddress(cp.getIpAddress())
                .necessary(cp.getNecessary())
                .analytics(cp.getAnalytics())
                .marketing(cp.getMarketing())
                .personalization(cp.getPersonalization())
                .consentGiven(cp.getConsentGiven())
                .consentVersion(cp.getConsentVersion())
                .consentDate(cp.getConsentDate())
                .updatedAt(cp.getUpdatedAt())
                .revokedAt(cp.getRevokedAt())
                .build();
    }
}
