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

    /** Abonelik planı: FREE, PLUS, PREMIUM. Mesaj limiti ve ek (fotoğraf/dosya/ses) özellikleri buna göre belirlenir. */
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    /** Premium/Plus aboneliğin bitiş tarihi; null ise süresiz veya FREE. */
    @Column(name = "subscription_ends_at")
    private LocalDateTime subscriptionEndsAt;

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
