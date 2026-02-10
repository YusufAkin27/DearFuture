package example.DearFuture.scheduler;

import example.DearFuture.mail.EmailMessage;
import example.DearFuture.mail.FutureMessageEmailTemplate;
import example.DearFuture.mail.MailService;
import example.DearFuture.message.entity.*;
import example.DearFuture.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final MailService mailService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Mesaj iletim zamanı geldiğinde, her alıcıya mesaj görüntüleme linki içeren HTML e-posta gönderir.
     * Mesaj içeriği doğrudan e-postada gönderilmez; alıcı linke tıklayarak özel sayfada görüntüler.
     */
    @Transactional(readOnly = true)
    public void send(FutureMessage message) {
        String subject = "Gelecekten bir mesajın var!";

        // Gönderen adını belirle
        User sender = message.getUser();
        String senderName = null;
        if (sender != null) {
            String first = sender.getFirstName();
            String last = sender.getLastName();
            if (first != null && !first.isBlank()) {
                senderName = first.trim();
                if (last != null && !last.isBlank()) {
                    senderName += " " + last.trim();
                }
            }
        }

        // Mesaj görüntüleme URL'i oluştur
        String baseUrl = frontendUrl != null && !frontendUrl.isBlank()
                ? frontendUrl.trim().replaceAll("/$", "")
                : "http://localhost:5173";
        String viewUrl = baseUrl + "/message/view/" + message.getViewToken();

        // HTML email oluştur
        String htmlBody = FutureMessageEmailTemplate.build(senderName, viewUrl);

        for (String recipient : message.getRecipientEmails()) {
            EmailMessage emailMessage = EmailMessage.builder()
                    .toEmail(recipient)
                    .subject(subject)
                    .body(htmlBody)
                    .isHtml(true)
                    .build();

            mailService.enqueueEmail(emailMessage);

            log.info("FutureMessage id={} view link queued for {}, token={}",
                    message.getId(), recipient, message.getViewToken());
        }
    }
}
