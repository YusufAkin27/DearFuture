package example.DearFuture.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Kullanıcının abonelik durumuna göre bu dönem kalan mesaj kullanım hakkı.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageResponse {

    private String planCode;
    private String planName;
    /** Bu dönem veya toplam maksimum mesaj hakkı */
    private int limit;
    /** Kullanılan mesaj sayısı */
    private int used;
    /** Kalan hak */
    private int remaining;
    /** Her ay yenileniyor mu (FREE: false, PLUS/PREMIUM: true) */
    private boolean resetsMonthly;
    /** Dönem başlangıcı (FREE için null) */
    private Instant periodStart;
    /** Dönem bitişi (FREE için null) */
    private Instant periodEnd;
}
