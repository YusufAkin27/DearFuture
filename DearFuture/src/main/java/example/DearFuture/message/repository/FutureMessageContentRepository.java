package example.DearFuture.message.repository;

import example.DearFuture.message.entity.FutureMessageContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FutureMessageContentRepository extends JpaRepository<FutureMessageContent, Long> {
}
