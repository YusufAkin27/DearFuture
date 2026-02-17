package example.DearFuture.message.repository;

import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.MessageStatus;
import example.DearFuture.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /** İletim için hazır mesajları içerikleriyle birlikte getirir (şifre çözümü için). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT DISTINCT fm FROM FutureMessage fm
                LEFT JOIN FETCH fm.contents
                WHERE fm.status = :status
                AND fm.scheduledAt <= :now
                AND fm.sentAt IS NULL
            """)
    List<FutureMessage> findReadyMessagesWithContents(
            @Param("status") MessageStatus status,
            @Param("now") Instant now);

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

    /** Id ile mesajı bul (içerikleriyle birlikte, admin detay için) */
    @Query("SELECT DISTINCT fm FROM FutureMessage fm LEFT JOIN FETCH fm.contents WHERE fm.id = :id")
    Optional<FutureMessage> findByIdWithContents(@Param("id") Long id);

    /** Kullanıcının belirli tarih aralığında scheduledAt değeri bu aralıkta olan mesaj sayısı (ücretli plan dönem kullanımı). */
    @Query("SELECT COUNT(fm) FROM FutureMessage fm WHERE fm.user.id = :userId AND fm.scheduledAt >= :start AND fm.scheduledAt <= :end")
    long countByUserIdAndScheduledAtBetween(@Param("userId") Long userId, @Param("start") Instant start, @Param("end") Instant end);

    /** Açılmış ve herkese açık mesajlar (public sayfa için). QUEUED/SENT = gönderilmiş/açılmış. */
    @Query("""
            SELECT DISTINCT fm FROM FutureMessage fm
            LEFT JOIN FETCH fm.contents
            WHERE fm.status IN ('SENT', 'QUEUED')
            AND fm.isPublic = true
            AND fm.scheduledAt <= :now
            ORDER BY fm.sentAt DESC, fm.scheduledAt DESC
            """)
    List<FutureMessage> findPublicMessagesUnlocked(Instant now);

    /** Sayfalı: açılmış ve herkese açık mesajlar. */
    @Query(value = """
            SELECT DISTINCT fm FROM FutureMessage fm
            LEFT JOIN FETCH fm.contents
            WHERE fm.status IN ('SENT', 'QUEUED')
            AND fm.isPublic = true
            AND fm.scheduledAt <= :now
            ORDER BY fm.sentAt DESC, fm.scheduledAt DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT fm) FROM FutureMessage fm
            WHERE fm.status IN ('SENT', 'QUEUED')
            AND fm.isPublic = true
            AND fm.scheduledAt <= :now
            """)
    org.springframework.data.domain.Page<FutureMessage> findPublicMessagesUnlocked(Instant now, Pageable pageable);
}
