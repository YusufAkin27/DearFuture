package example.DearFuture.auth.repository;


import example.DearFuture.auth.entity.LoginCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LoginCodeRepository extends JpaRepository<LoginCode, Long> {

    @Query("SELECT lc FROM LoginCode lc WHERE lc.email = :email AND lc.code = :code AND lc.expiresAt > :now AND lc.used = false")
    Optional<LoginCode> findValidCode(String email, String code, LocalDateTime now);

    Optional<LoginCode> findTopByEmailOrderByCreatedAtDesc(String email);
}
