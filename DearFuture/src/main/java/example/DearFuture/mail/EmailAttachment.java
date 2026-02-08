package example.DearFuture.mail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailAttachment {

    private String fileName;
    private String fileUrl;
    private Long fileSize;
}
