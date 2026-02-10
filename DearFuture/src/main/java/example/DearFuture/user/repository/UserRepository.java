package example.DearFuture.user.repository;

import example.DearFuture.user.entity.User;
import example.DearFuture.user.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    /** Süresi dolmuş ve belirli planlarda olan kullanıcılar */
    @Query("SELECT u FROM User u WHERE u.subscriptionEndsAt < :date AND u.subscriptionPlan IN :plans")
    List<User> findBySubscriptionEndsAtBeforeAndSubscriptionPlanIn(LocalDateTime date, List<SubscriptionPlan> plans);

    /** Süresi dolmuş ücretli abonelikleri bul (otomatik yenileme için) - FREE olmayan planlar */
    @Query("SELECT u FROM User u JOIN u.subscriptionPlan sp WHERE u.subscriptionEndsAt < :date AND sp.code <> :excludedPlanCode")
    List<User> findExpiredPaidSubscriptions(LocalDateTime date, String excludedPlanCode);

    /** Plan bazında kullanıcı sayısı */
    long countBySubscriptionPlan(SubscriptionPlan plan);
}
