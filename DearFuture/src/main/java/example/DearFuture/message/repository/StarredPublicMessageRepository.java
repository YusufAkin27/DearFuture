package example.DearFuture.message.repository;

import example.DearFuture.message.entity.StarredPublicMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StarredPublicMessageRepository extends JpaRepository<StarredPublicMessage, Long> {

    @Modifying
    @Query("DELETE FROM StarredPublicMessage s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndFutureMessageId(Long userId, Long futureMessageId);

    Optional<StarredPublicMessage> findByUserIdAndFutureMessageId(Long userId, Long futureMessageId);

    List<StarredPublicMessage> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Yıldızlı mesaj id'leri (N+1 önlemek için; public liste starredByMe için) */
    @Query("SELECT s.futureMessage.id FROM StarredPublicMessage s WHERE s.user.id = :userId")
    List<Long> findFutureMessageIdsByUserId(@Param("userId") Long userId);

    /** Yıldızlı kayıtları mesaj ile birlikte getirir (N+1 önlemek için) */
    @Query("SELECT s FROM StarredPublicMessage s JOIN FETCH s.futureMessage WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<StarredPublicMessage> findByUserIdWithFutureMessageOrderByCreatedAtDesc(@Param("userId") Long userId);

    void deleteByUserIdAndFutureMessageId(Long userId, Long futureMessageId);
}
