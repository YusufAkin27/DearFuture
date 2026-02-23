package example.DearFuture.message.dto.response;

import example.DearFuture.message.entity.ContentType;
import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.FutureMessageContent;
import example.DearFuture.message.entity.MessageStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class MessageResponse {
    private static final String ENCRYPTED_PREFIX = "ENCv1:";

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

    /** İçerik türleri: TEXT, IMAGE, VIDEO, FILE, AUDIO (liste önizlemesi için) */
    private List<String> contentTypes;

    /** Liste önizlemesi için ilk fotoğraf URL'i (varsa) */
    private String previewImageUrl;

    public static MessageResponse fromEntity(FutureMessage message) {
        String contentText = "";
        List<String> types = List.of();
        String previewImageUrl = null;
        if (message.getContents() != null && !message.getContents().isEmpty()) {
            contentText = message.getContents().get(0).getTextContent();
            types = message.getContents().stream()
                    .map(FutureMessageContent::getType)
                    .map(Enum::name)
                    .distinct()
                    .collect(Collectors.toList());
            previewImageUrl = message.getContents().stream()
                    .filter(c -> c.getType() == ContentType.IMAGE && c.getFileUrl() != null && !c.getFileUrl().isBlank())
                    .map(FutureMessageContent::getFileUrl)
                    .findFirst()
                    .orElse(null);
        }
        if (contentText != null && contentText.startsWith(ENCRYPTED_PREFIX)) {
            contentText = "[Şifreli içerik]";
        } else if (contentText == null) {
            contentText = "";
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
                .contentTypes(types)
                .previewImageUrl(previewImageUrl)
                .build();
    }
}
