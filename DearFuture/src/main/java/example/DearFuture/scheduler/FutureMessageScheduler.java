package example.DearFuture.scheduler;

import example.DearFuture.message.encryption.MessageEncryptionService;
import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.MessageStatus;
import example.DearFuture.message.repository.FutureMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FutureMessageScheduler {

    private final FutureMessageRepository repository;
    private final MessageSender messageSender;
    private final MessageEncryptionService messageEncryptionService;

    /**
     * Zamanı gelen mesajları kilitler, içerikleriyle yükler, şifre çözer ve mail kuyruğuna ekler.
     * Önce basit SELECT FOR UPDATE (findReadyMessages) kullanılıyor; JOIN FETCH + PESSIMISTIC_WRITE
     * PostgreSQL'de follow-on locking uyarısına yol açtığı için içerikler ayrı sorguda yükleniyor.
     */
    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void processScheduledMessages() {
        List<FutureMessage> messages = repository.findReadyMessages(
                MessageStatus.SCHEDULED,
                Instant.now()
        );

        for (FutureMessage message : messages) {
            FutureMessage withContents = repository.findByIdWithContents(message.getId())
                    .orElseThrow(() -> new IllegalStateException("Message not found: " + message.getId()));
            messageEncryptionService.decryptMessageContents(withContents);
            repository.save(withContents);
            withContents.setStatus(MessageStatus.QUEUED);
            withContents.setSentAt(Instant.now());
            repository.save(withContents);
            messageSender.send(withContents);
        }
    }
}
