package example.DearFuture.message.dto.request;

import example.DearFuture.message.entity.ContentType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageContentRequest {
    @NotNull
    private ContentType type;

    private String text;

    private String fileUrl;
    private String fileName;
    private Long fileSize;
}
