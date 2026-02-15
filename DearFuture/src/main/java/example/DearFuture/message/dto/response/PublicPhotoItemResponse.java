package example.DearFuture.message.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Herkese açık mesajlardaki tek bir fotoğraf içeriği (sayfalı liste için).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicPhotoItemResponse {

    private Long contentId;
    private Long messageId;
    private String viewToken;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Instant sentAt;
    private String senderName;
}
