package example.DearFuture.payment.dto.response;

import example.DearFuture.user.entity.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Plan detay sayfası için tek plan bilgisi (açıklama, fiyat, limitler).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDetailResponse {

    private String id;
    private String name;
    private String description;
    private int price;
    private String priceLabel;
    private List<String> features;
    private boolean recommended;
    private boolean active;

    // Limitler
    private int maxMessages;
    private int maxRecipientsPerMessage;
    private boolean allowPhoto;
    private boolean allowFile;
    private boolean allowVoice;
    private int maxPhotosPerMessage;
    private long maxPhotoSizeBytes;
    private int maxFilesPerMessage;
    private long maxFileSizeBytes;
    private int maxAudioPerMessage;
    private long maxAudioSizeBytes;

    private int displayOrder;

    public static PlanDetailResponse fromEntity(SubscriptionPlan p) {
        if (p == null) return null;
        return PlanDetailResponse.builder()
                .id(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getMonthlyPrice() != null ? p.getMonthlyPrice().intValue() : 0)
                .priceLabel(p.getPriceLabel() != null ? p.getPriceLabel() : "₺/ay")
                .features(p.getFeatures())
                .recommended(p.isRecommended())
                .active(p.isActive())
                .maxMessages(p.getMaxMessages())
                .maxRecipientsPerMessage(p.getMaxRecipientsPerMessage())
                .allowPhoto(p.isAllowPhoto())
                .allowFile(p.isAllowFile())
                .allowVoice(p.isAllowVoice())
                .maxPhotosPerMessage(p.getMaxPhotosPerMessage())
                .maxPhotoSizeBytes(p.getMaxPhotoSizeBytes())
                .maxFilesPerMessage(p.getMaxFilesPerMessage())
                .maxFileSizeBytes(p.getMaxFileSizeBytes())
                .maxAudioPerMessage(p.getMaxAudioPerMessage())
                .maxAudioSizeBytes(p.getMaxAudioSizeBytes())
                .displayOrder(p.getDisplayOrder())
                .build();
    }
}
