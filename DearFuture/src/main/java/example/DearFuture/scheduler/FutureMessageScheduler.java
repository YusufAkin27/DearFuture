package example.DearFuture.scheduler;

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
    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void processScheduledMessages() {

        List<FutureMessage> messages =
                repository.findReadyMessages(
                        MessageStatus.SCHEDULED,
                        Instant.now()
                );

        for (FutureMessage message : messages) {
            messageSender.send(message);
            message.setStatus(MessageStatus.QUEUED);
            message.setSentAt(Instant.now());
            repository.save(message);
        }
    }

}
