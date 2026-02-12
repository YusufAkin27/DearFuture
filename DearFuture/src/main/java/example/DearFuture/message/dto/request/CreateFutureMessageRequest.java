package example.DearFuture.message.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateFutureMessageRequest {

    @NotEmpty(message = "Recipient email list cannot be empty")
    private List<
            @NotEmpty
            @Email(message = "Invalid email format")
                    String
            > recipientEmails;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Scheduled time must be in the future")
    private Instant scheduledAt;

    @NotEmpty(message = "Message contents cannot be empty")
    @Valid
    private List<MessageContentRequest> contents;

    /** true = Herkes okuyabilsin (açıldıktan sonra public sayfada listelenir) */
    private Boolean isPublic;
}

