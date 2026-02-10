package example.DearFuture.admin.config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * User plan migration'ı ayrı bir transaction'da çalıştırır.
 * Hata olursa sadece bu transaction rollback olur, ana başlangıç transaction'ı etkilenmez.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanMigrationHelper {

    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void migrateExistingUsersToPlanEntity() throws Exception {
        try {
            int updated = entityManager.createNativeQuery(
                    "UPDATE users u SET subscription_plan_id = " +
                    "(SELECT sp.id FROM subscription_plans sp WHERE sp.code = u.subscription_plan) " +
                    "WHERE u.subscription_plan_id IS NULL AND u.subscription_plan IS NOT NULL"
            ).executeUpdate();

            if (updated > 0) {
                log.info("Migrated {} existing users to new plan entity references.", updated);
            }
        } catch (Exception e) {
            log.warn("User migration skipped (old subscription_plan column may not exist): {}", e.getMessage());
            throw e;
        }
    }
}
