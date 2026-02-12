package example.DearFuture.message.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Herkese açık mesaj listesi öğesi (public sayfa).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicMessageItemResponse {

    private Long id;
    private String viewToken;
    private Instant scheduledAt;
    private Instant sentAt;
    private String senderName;
    /** Metin önizlemesi (ilk metin içeriği, en fazla ~200 karakter) */
    private String textPreview;
    /** Giriş yapan kullanıcı bu mesajı yıldızlamış mı */
    private Boolean starredByMe;
}
