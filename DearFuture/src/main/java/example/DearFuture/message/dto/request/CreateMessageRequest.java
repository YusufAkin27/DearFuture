package example.DearFuture.message.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateMessageRequest {

    @NotBlank(message = "Content cannot be empty")
    private String content;

    @NotNull(message = "Scheduled date is required")
    @Future(message = "Date must be in the future")
    private Instant scheduledAt;

    /** true = Herkes okuyabilsin (açıldıktan sonra public sayfada listelenir) */
    private Boolean isPublic;
}
