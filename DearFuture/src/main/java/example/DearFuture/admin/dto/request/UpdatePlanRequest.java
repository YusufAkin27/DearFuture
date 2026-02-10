package example.DearFuture.admin.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Admin tarafından plan güncelleme isteği. Tüm alanlar opsiyonel (sadece gönderilen güncellenir).
 */
@Data
public class UpdatePlanRequest {
    private String name;
    private String description;
    private BigDecimal monthlyPrice;
    private String priceLabel;

    private Integer maxMessages;
    private Integer maxRecipientsPerMessage;
    private Boolean allowPhoto;
    private Boolean allowFile;
    private Boolean allowVoice;
    private Integer maxPhotosPerMessage;
    private Long maxPhotoSizeBytes;
    private Integer maxFilesPerMessage;
    private Long maxFileSizeBytes;

    private List<String> features;
    private Boolean recommended;
    private Boolean active;
    private Integer displayOrder;
}
