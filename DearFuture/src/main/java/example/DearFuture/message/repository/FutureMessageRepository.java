package example.DearFuture.message.repository;

import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.MessageStatus;
import example.DearFuture.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FutureMessageRepository extends JpaRepository<FutureMessage, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT fm FROM FutureMessage fm
                WHERE fm.status = :status
                AND fm.scheduledAt <= :now
                AND fm.sentAt IS NULL
            """)
    List<FutureMessage> findReadyMessages(
            MessageStatus status,
            Instant now);

    List<FutureMessage> findAllByUser(User user);

    List<FutureMessage> findAllByUserAndScheduledAtAfterOrderByScheduledAtAsc(User user, Instant now);

    List<FutureMessage> findAllByUserAndScheduledAtBeforeOrderByScheduledAtDesc(User user, Instant now);

    @Query("SELECT DISTINCT fm FROM FutureMessage fm LEFT JOIN FETCH fm.contents WHERE fm.user = :user AND fm.scheduledAt > :now ORDER BY fm.scheduledAt ASC")
    List<FutureMessage> findAllByUserAndScheduledAtAfterOrderByScheduledAtAscWithContents(User user, Instant now);

    @Query("SELECT DISTINCT fm FROM FutureMessage fm LEFT JOIN FETCH fm.contents WHERE fm.user = :user AND fm.scheduledAt <= :now ORDER BY fm.scheduledAt DESC")
    List<FutureMessage> findAllByUserAndScheduledAtBeforeOrderByScheduledAtDescWithContents(User user, Instant now);

    /** Kullanıcının SCHEDULED durumundaki mesaj sayısı (plan limiti kontrolü için). */
    long countByUserAndStatus(User user, MessageStatus status);

    /** Kullanıcının belirtilen durumlardaki mesaj sayısı (FREE: bekleyen + iletilen toplamı için). */
    long countByUserAndStatusIn(User user, Set<MessageStatus> statuses);

    /** viewToken ile mesajı bul (içerikleriyle birlikte) */
    @Query("SELECT DISTINCT fm FROM FutureMessage fm LEFT JOIN FETCH fm.contents WHERE fm.viewToken = :viewToken")
    Optional<FutureMessage> findByViewTokenWithContents(String viewToken);

}
