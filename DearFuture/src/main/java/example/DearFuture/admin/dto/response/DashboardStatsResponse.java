package example.DearFuture.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    /** Toplam kullanıcı sayısı */
    private long totalUsers;

    /** Toplam mesaj sayısı */
    private long totalMessages;

    /** Toplam ödeme sayısı (başarılı) */
    private long totalPayments;

    /** Plan bazında kullanıcı sayıları: { "FREE": 120, "PLUS": 45, "PREMIUM": 12 } */
    private Map<String, Long> usersPerPlan;

    /** Mesaj durumlarına göre sayılar: { "SCHEDULED": 50, "SENT": 200, "QUEUED": 5 } */
    private Map<String, Long> messagesPerStatus;
}
