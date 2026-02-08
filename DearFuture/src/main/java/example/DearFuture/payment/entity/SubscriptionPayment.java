package example.DearFuture.payment.entity;

import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Üyelik ödemesi kaydı. Her başarılı ödeme ve (ileride) her ay otomatik tahsilat için kayıt.
 */
@Entity
@Table(name = "subscription_payments", indexes = {
        @Index(name = "idx_sub_payment_user_status", columnList = "user_id, status"),
        @Index(name = "idx_sub_payment_conversation", columnList = "conversation_id"),
        @Index(name = "idx_sub_payment_token", columnList = "checkout_token")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private SubscriptionPlan plan;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "conversation_id", nullable = false, unique = true)
    private String conversationId;

    /** iyzico checkout form token (callback'te bu ile arama yapılabilir) */
    @Column(name = "checkout_token")
    private String checkoutToken;

    @Column(name = "iyzico_payment_id")
    private String iyzicoPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /** Ödeme dönemi başlangıcı */
    @Column(name = "period_start")
    private LocalDateTime periodStart;

    /** Ödeme dönemi bitişi (aboneliğin geçerlilik süresi) */
    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Otomatik yenileme mi (true) yoksa ilk satın alma mı (false) */
    @Column(name = "is_renewal")
    private boolean renewal;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public enum PaymentStatus {
        PENDING,
        SUCCESS,
        FAILED
    }
}
