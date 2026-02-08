package example.DearFuture.mail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {
    private String toEmail;
    private String subject;
    private String body;
    private boolean isHtml;
    private List<EmailAttachment> attachments;
}
