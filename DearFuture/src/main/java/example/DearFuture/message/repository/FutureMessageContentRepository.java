package example.DearFuture.message.repository;

import example.DearFuture.message.entity.ContentType;
import example.DearFuture.message.entity.FutureMessageContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface FutureMessageContentRepository extends JpaRepository<FutureMessageContent, Long> {

    /**
     * Açılmış ve herkese açık mesajlardaki sadece fotoğraf (IMAGE) içeriklerini sayfalı getirir.
     */
    @Query(value = """
            SELECT DISTINCT c FROM FutureMessageContent c
            JOIN FETCH c.futureMessage fm
            LEFT JOIN FETCH fm.user
            WHERE fm.isPublic = true
            AND fm.status IN ('SENT', 'QUEUED')
            AND fm.scheduledAt <= :now
            AND c.type = :type
            AND c.fileUrl IS NOT NULL
            ORDER BY fm.sentAt DESC NULLS LAST, c.id DESC
            """,
            countQuery = """
            SELECT COUNT(c) FROM FutureMessageContent c
            JOIN c.futureMessage fm
            WHERE fm.isPublic = true
            AND fm.status IN ('SENT', 'QUEUED')
            AND fm.scheduledAt <= :now
            AND c.type = :type
            AND c.fileUrl IS NOT NULL
            """)
    Page<FutureMessageContent> findPublicPhotoContents(@Param("now") Instant now, @Param("type") ContentType type, Pageable pageable);
}
