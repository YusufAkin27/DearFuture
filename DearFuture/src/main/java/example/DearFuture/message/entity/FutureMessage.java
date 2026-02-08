package example.DearFuture.message.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(
        name = "future_messages",
        indexes = {
                @Index(name = "idx_future_message_status_scheduled", columnList = "status, scheduled_at")
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
    private example.DearFuture.user.entity.User user;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

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
