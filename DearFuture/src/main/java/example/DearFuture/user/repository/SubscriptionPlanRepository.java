package example.DearFuture.user.repository;

import example.DearFuture.user.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByCode(String code);

    boolean existsByCode(String code);

    /** Aktif planları sıralı getir (fiyatlandırma sayfası için) */
    List<SubscriptionPlan> findByActiveTrueOrderByDisplayOrderAsc();

    /** Tüm planları sıralı getir (admin paneli için) */
    List<SubscriptionPlan> findAllByOrderByDisplayOrderAsc();

    /** Her plandaki kullanıcı sayısı */
    @Query("SELECT sp.code, COUNT(u) FROM User u RIGHT JOIN u.subscriptionPlan sp GROUP BY sp.id, sp.code ORDER BY sp.displayOrder")
    List<Object[]> countUsersPerPlan();
}
