package example.DearFuture.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class EmailQueue {

    private final ObjectMapper objectMapper;

    private final Queue<String> emailQueue = new ConcurrentLinkedQueue<>();

    // Constructor
    public EmailQueue(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void enqueue(EmailMessage email) {
        try {
            String json = objectMapper.writeValueAsString(email);
            emailQueue.offer(json);
            log.info("Kuyruğa e-posta eklendi: {}", email.getToEmail());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize email message", e);
        }
    }

    public String dequeue() {
        return emailQueue.poll();
    }

    public boolean isEmpty() {
        return emailQueue.isEmpty();
    }
}
