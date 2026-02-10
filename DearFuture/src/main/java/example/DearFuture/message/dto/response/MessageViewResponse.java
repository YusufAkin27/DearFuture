package example.DearFuture.message.dto.response;

import example.DearFuture.message.entity.ContentType;
import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.FutureMessageContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Alıcıya özel mesaj görüntüleme sayfası için DTO.
 * Token ile erişilen public endpoint'ten döner.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageViewResponse {

    /** Mesajın gönderildiği tarih */
    private Instant scheduledAt;

    /** Gönderenin adı (varsa) */
    private String senderName;

    /** Mesaj içerikleri (metin, fotoğraf, ses, dosya vb.) */
    private List<ContentItem> contents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentItem {
        private ContentType type;

        /** TEXT tipinde metin içeriği */
        private String textContent;

        /** IMAGE, FILE, VIDEO, AUDIO tipinde dosya URL'i */
        private String fileUrl;

        /** Dosya adı */
        private String fileName;

        /** Dosya boyutu (byte) */
        private Long fileSize;

        public static ContentItem fromEntity(FutureMessageContent content) {
            return ContentItem.builder()
                    .type(content.getType())
                    .textContent(content.getTextContent())
                    .fileUrl(content.getFileUrl())
                    .fileName(content.getFileName())
                    .fileSize(content.getFileSize())
                    .build();
        }
    }

    public static MessageViewResponse fromEntity(FutureMessage message) {
        String senderName = null;
        if (message.getUser() != null) {
            String first = message.getUser().getFirstName();
            String last = message.getUser().getLastName();
            if (first != null && !first.isBlank()) {
                senderName = first.trim();
                if (last != null && !last.isBlank()) {
                    senderName += " " + last.trim();
                }
            }
        }

        List<ContentItem> contentItems = message.getContents() != null
                ? message.getContents().stream()
                    .map(ContentItem::fromEntity)
                    .collect(Collectors.toList())
                : List.of();

        return MessageViewResponse.builder()
                .scheduledAt(message.getScheduledAt())
                .senderName(senderName)
                .contents(contentItems)
                .build();
    }
}
