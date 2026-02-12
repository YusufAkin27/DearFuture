package example.DearFuture.message.entity;

import example.DearFuture.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Kullanıcının "herkese açık" bir mesajı yıldızlaması.
 * Bir kullanıcı aynı mesajı yalnızca bir kez yıldızlayabilir.
 */
@Entity
@Table(
        name = "starred_public_messages",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "future_message_id"})
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StarredPublicMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "future_message_id", nullable = false)
    private FutureMessage futureMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
