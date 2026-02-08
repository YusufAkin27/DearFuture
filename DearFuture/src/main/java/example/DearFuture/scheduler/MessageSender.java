package example.DearFuture.scheduler;

import example.DearFuture.mail.EmailAttachment;
import example.DearFuture.mail.EmailMessage;
import example.DearFuture.mail.MailService;
import example.DearFuture.message.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final MailService mailService;

    @Transactional(readOnly = true)
    public void send(FutureMessage message) {

        String subject = "Gelecekten bir mesajın var ✉️";

        StringBuilder bodyBuilder = new StringBuilder();
        List<EmailAttachment> attachments = new ArrayList<>();

        for (FutureMessageContent content : message.getContents()) {

            if (content.getType() == ContentType.TEXT) {
                bodyBuilder
                        .append(content.getTextContent())
                        .append("\n\n");
            } else {
                // Dosya link olarak body'e girsin
                bodyBuilder
                        .append("📎 ")
                        .append(content.getFileName())
                        .append("\n")
                        .append(content.getFileUrl())
                        .append("\n\n");

                attachments.add(
                        EmailAttachment.builder()
                                .fileName(content.getFileName())
                                .fileUrl(content.getFileUrl())
                                .fileSize(content.getFileSize())
                                .build()
                );
            }
        }

        String body = bodyBuilder.toString();

        for (String recipient : message.getRecipientEmails()) {

            EmailMessage emailMessage = EmailMessage.builder()
                    .toEmail(recipient)
                    .subject(subject)
                    .body(body)
                    .isHtml(false) // ileride true yapabilirsin
                    .attachments(attachments)
                    .build();

            mailService.enqueueEmail(emailMessage);

            log.info(
                    "FutureMessage id={} queued for {}",
                    message.getId(),
                    recipient
            );
        }
    }
}
