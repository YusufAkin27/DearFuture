package example.DearFuture.message.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageUploadResponse {
    private String url;
    private String fileName;
    private Long fileSize;
}
