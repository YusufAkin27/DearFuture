package example.DearFuture.admin.dto.response;

import example.DearFuture.message.entity.ContentType;
import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.FutureMessageContent;
import example.DearFuture.message.entity.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMessageResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private Instant scheduledAt;
    private Instant sentAt;
    private MessageStatus status;
    private String viewToken;
    private List<String> recipientEmails;
    private List<ContentSummary> contents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentSummary {
        private ContentType type;
        private String textPreview;
        private String fileName;
        private Long fileSize;
    }

    public static AdminMessageResponse fromEntity(FutureMessage msg) {
        String userName = null;
        if (msg.getUser() != null) {
            String f = msg.getUser().getFirstName();
            String l = msg.getUser().getLastName();
            if (f != null && !f.isBlank()) {
                userName = f.trim() + (l != null && !l.isBlank() ? " " + l.trim() : "");
            }
        }

        List<ContentSummary> contentList = msg.getContents() != null
                ? msg.getContents().stream().map(c -> ContentSummary.builder()
                    .type(c.getType())
                    .textPreview(c.getType() == ContentType.TEXT && c.getTextContent() != null
                            ? c.getTextContent().substring(0, Math.min(c.getTextContent().length(), 200))
                            : null)
                    .fileName(c.getFileName())
                    .fileSize(c.getFileSize())
                    .build())
                .collect(Collectors.toList())
                : List.of();

        return AdminMessageResponse.builder()
                .id(msg.getId())
                .userId(msg.getUser() != null ? msg.getUser().getId() : null)
                .userEmail(msg.getUser() != null ? msg.getUser().getEmail() : null)
                .userName(userName)
                .scheduledAt(msg.getScheduledAt())
                .sentAt(msg.getSentAt())
                .status(msg.getStatus())
                .viewToken(msg.getViewToken())
                .recipientEmails(msg.getRecipientEmails())
                .contents(contentList)
                .build();
    }
}
