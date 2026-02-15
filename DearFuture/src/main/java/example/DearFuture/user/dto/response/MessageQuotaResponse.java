package example.DearFuture.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kullanıcının planına göre mesaj kotası: toplam kullanılan ve kalan hak.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageQuotaResponse {

    /** Plan limiti (bu dönem veya toplam maksimum mesaj). */
    private long limit;
    /** Kullanılmış mesaj sayısı (bu dönem veya toplam). */
    private long used;
    /** Kalan mesaj hakkı. */
    private long remaining;
    /** Plan kodu: FREE, PLUS, PREMIUM. */
    private String planCode;
    /** Plan adı. */
    private String planName;
}
