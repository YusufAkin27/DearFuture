package example.DearFuture.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User   {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private boolean emailVerified = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    private Set<Role> roles;

    private boolean enabled = true;

    private String firstName;
    private String lastName;

    private String profilePictureUrl;

    /** Abonelik planı. Mesaj limiti ve ek (fotoğraf/dosya/ses) özellikleri buna göre belirlenir. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    /** Premium/Plus aboneliğin bitiş tarihi; null ise süresiz veya FREE. */
    @Column(name = "subscription_ends_at")
    private LocalDateTime subscriptionEndsAt;

    /** iyzico kart saklama - cardUserKey */
    @Column(name = "card_user_key")
    private String cardUserKey;

    /** iyzico kart saklama - cardToken */
    @Column(name = "card_token")
    private String cardToken;

    /** Ardışık başarısız ödeme denemesi sayısı */
    @Column(name = "consecutive_payment_failures", nullable = false)
    private int consecutivePaymentFailures = 0;

    /** İlk başarısız ödeme tarihi (7 gün hesabı için) */
    @Column(name = "payment_failed_since")
    private LocalDateTime paymentFailedSince;

    private LocalDateTime createdAt;

    /** Dil kodu: tr, en vb. */
    @Column(name = "locale", length = 10)
    private String locale = "tr";

    /** E-posta ile bildirimler (mesaj hatırlatmaları vb.) */
    @Column(name = "email_notifications", nullable = false)
    private boolean emailNotifications = true;

    /** Pazarlama / kampanya e-postaları */
    @Column(name = "marketing_emails", nullable = false)
    private boolean marketingEmails = false;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    public boolean isEnabled() { return enabled; }
}
