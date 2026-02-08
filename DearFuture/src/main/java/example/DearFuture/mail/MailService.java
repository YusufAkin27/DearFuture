package example.DearFuture.mail;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final EmailQueue emailQueue;

    private final String fromEmail = "no-reply@yusufakin.online";

    /**
     * Kuyruğu kontrol et ve mail gönder
     * 5 saniyede bir çalışacak
     */
    @Scheduled(fixedDelay = 5000)
    public void processQueue() {
        while (!emailQueue.isEmpty()) {
            String json = emailQueue.dequeue();
            if (json == null)
                continue;

            try {
                EmailMessage email = EmailMessageMapper.fromJson(json);

                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(email.getToEmail());
                helper.setSubject(email.getSubject());
                helper.setText(email.getBody(), email.isHtml());

                if (email.getAttachments() != null) {
                    for (EmailAttachment attachment : email.getAttachments()) {
                        try {
                            UrlResource resource = new UrlResource(attachment.getFileUrl());
                            helper.addAttachment(attachment.getFileName(), resource);
                        } catch (MalformedURLException e) {
                            log.error("Invalid attachment URL: {}", attachment.getFileUrl(), e);
                        }
                    }
                }

                mailSender.send(mimeMessage);
                log.info("E-posta gönderildi: {}", email.getToEmail());

            } catch (Exception e) {
                log.error("E-posta gönderilemedi: {}", e.getMessage(), e);
            }
        }
    }

    public void enqueueEmail(EmailMessage emailMessage) {
        emailQueue.enqueue(emailMessage);
        log.info("E-posta kuyruğa eklendi: {}", emailMessage.getToEmail());
    }
}
