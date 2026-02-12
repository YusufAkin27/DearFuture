package example.DearFuture.admin.dto.response;

import example.DearFuture.payment.entity.SubscriptionPayment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaymentResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String planCode;
    private BigDecimal amount;
    private SubscriptionPayment.PaymentStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private boolean renewal;
    private String conversationId;

    public static AdminPaymentResponse fromEntity(SubscriptionPayment p) {
        String userEmail = p.getUser() != null ? p.getUser().getEmail() : null;
        return AdminPaymentResponse.builder()
                .id(p.getId())
                .userId(p.getUser() != null ? p.getUser().getId() : null)
                .userEmail(userEmail)
                .planCode(p.getPlanCode())
                .amount(p.getAmount())
                .status(p.getStatus())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .renewal(p.isRenewal())
                .conversationId(p.getConversationId())
                .build();
    }
}
