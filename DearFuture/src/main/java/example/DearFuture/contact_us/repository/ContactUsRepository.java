package example.DearFuture.contact_us.repository;

import example.DearFuture.contact_us.entity.ContactUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContactUsRepository extends JpaRepository<ContactUs, Long> {

    List<ContactUs> findByVerifiedTrue();

    Optional<ContactUs> findFirstByEmailIgnoreCaseAndVerifiedFalseOrderByCreatedAtDesc(String email);

    @Query("SELECT COUNT(c) FROM ContactUs c WHERE LOWER(c.email) = LOWER(:email) AND c.createdAt > :since")
    long countByEmailAndCreatedAtAfter(@Param("email") String email, @Param("since") LocalDateTime since);
}
