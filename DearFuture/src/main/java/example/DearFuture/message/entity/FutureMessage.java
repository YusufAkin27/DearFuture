package example.DearFuture.message.entity;

import example.DearFuture.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "future_messages",
        indexes = {
                @Index(name = "idx_future_message_status_scheduled", columnList = "status, scheduled_at"),
                @Index(name = "idx_future_message_view_token", columnList = "view_token")
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FutureMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Her mesaja özel benzersiz token. Alıcı bu token ile mesajı görüntüler. */
    @Column(name = "view_token", nullable = false, unique = true, updatable = false)
    private String viewToken;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @PrePersist
    void onPersist() {
        if (viewToken == null) {
            viewToken = UUID.randomUUID().toString();
        }
    }

    /**
     * message_recipients tablosu
     */
    @ElementCollection
    @CollectionTable(
            name = "future_message_recipients",
            joinColumns = @JoinColumn(name = "future_message_id")
    )
    @Column(name = "email", nullable = false)
    private List<String> recipientEmails;

    /**
     * One FutureMessage -> Many Contents
     */
    @OneToMany(
            mappedBy = "futureMessage",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<FutureMessageContent> contents;
}
