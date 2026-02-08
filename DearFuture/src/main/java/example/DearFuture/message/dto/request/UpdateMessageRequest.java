package example.DearFuture.message.dto.request;

import jakarta.validation.constraints.Future;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateMessageRequest {
    private String content;

    @Future(message = "Scheduled date must be in the future")
    private Instant scheduledAt;
}
