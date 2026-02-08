package example.DearFuture.user.repository;

import example.DearFuture.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import example.DearFuture.user.entity.SubscriptionPlan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    /** Süresi dolmuş ücretli aboneliği olan kullanıcılar (plan FREE'ye düşürülecek) */
    List<User> findBySubscriptionEndsAtBeforeAndSubscriptionPlanIn(LocalDateTime date, List<SubscriptionPlan> plans);
}
