package example.DearFuture.admin.dto.response;

import example.DearFuture.user.entity.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDetailResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal monthlyPrice;
    private String priceLabel;

    private int maxMessages;
    private int maxRecipientsPerMessage;
    private boolean allowPhoto;
    private boolean allowFile;
    private boolean allowVoice;
    private int maxPhotosPerMessage;
    private long maxPhotoSizeBytes;
    private int maxFilesPerMessage;
    private long maxFileSizeBytes;

    private List<String> features;
    private boolean recommended;
    private boolean active;
    private int displayOrder;

    /** Bu plandaki kullanıcı sayısı (admin paneli için) */
    private long userCount;

    public static PlanDetailResponse fromEntity(SubscriptionPlan plan, long userCount) {
        return PlanDetailResponse.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .priceLabel(plan.getPriceLabel())
                .maxMessages(plan.getMaxMessages())
                .maxRecipientsPerMessage(plan.getMaxRecipientsPerMessage())
                .allowPhoto(plan.isAllowPhoto())
                .allowFile(plan.isAllowFile())
                .allowVoice(plan.isAllowVoice())
                .maxPhotosPerMessage(plan.getMaxPhotosPerMessage())
                .maxPhotoSizeBytes(plan.getMaxPhotoSizeBytes())
                .maxFilesPerMessage(plan.getMaxFilesPerMessage())
                .maxFileSizeBytes(plan.getMaxFileSizeBytes())
                .features(plan.getFeatures())
                .recommended(plan.isRecommended())
                .active(plan.isActive())
                .displayOrder(plan.getDisplayOrder())
                .userCount(userCount)
                .build();
    }
}
