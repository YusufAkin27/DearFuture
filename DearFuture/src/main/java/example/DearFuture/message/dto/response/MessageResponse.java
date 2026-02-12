package example.DearFuture.message.dto.response;

import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.MessageStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class MessageResponse {
    private Long id;
    private String content;
    private Instant scheduledAt;
    private Instant sentAt;
    private MessageStatus status;
    private List<String> recipientEmails;

    /** Mesajın görüntüleme token'ı (alıcı bu token ile mesajı açar) */
    private String viewToken;

    /** Herkes okuyabilsin seçeneği (açıldıktan sonra public sayfada listelenir) */
    private boolean isPublic;

    public static MessageResponse fromEntity(FutureMessage message) {
        String contentText = "";
        if (message.getContents() != null && !message.getContents().isEmpty()) {
            contentText = message.getContents().get(0).getTextContent();
        }

        return MessageResponse.builder()
                .id(message.getId())
                .content(contentText != null ? contentText : "")
                .scheduledAt(message.getScheduledAt())
                .sentAt(message.getSentAt())
                .status(message.getStatus())
                .recipientEmails(message.getRecipientEmails())
                .viewToken(message.getViewToken())
                .isPublic(message.isPublic())
                .build();
    }
}
