package example.DearFuture.contact_us.repository;

import example.DearFuture.contact_us.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findTopByCodeAndUsedFalseOrderByCreatedAtDesc(String code);

    List<EmailVerificationCode> findByEmailIgnoreCaseAndUsedFalse(String email);

    @Modifying
    @Query("UPDATE EmailVerificationCode e SET e.used = true WHERE e.expiresAt < :now AND e.used = false")
    int invalidateExpiredCodes(@Param("now") LocalDateTime now);
}
