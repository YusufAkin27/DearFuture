package example.DearFuture.admin.config;

import example.DearFuture.user.entity.Role;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.SubscriptionPlanRepository;
import example.DearFuture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

/**
 * Uygulama başlangıcında admin hesabını oluşturur veya mevcut hesaba ADMIN rolü ekler.
 * Admin e-posta: app.admin-email (varsayılan: ysufakn63@gmail.com)
 * Giriş: aynı API ile e-posta + doğrulama kodu (send-code → verify).
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;

    private static final String DEFAULT_ADMIN_EMAIL = "ysufakn63@gmail.com";

    @Override
    @Transactional
    public void run(String... args) {
        String adminEmail = System.getProperty("app.admin-email");
        if (adminEmail == null || adminEmail.isBlank()) {
            adminEmail = System.getenv("APP_ADMIN_EMAIL");
        }
        if (adminEmail == null || adminEmail.isBlank()) {
            adminEmail = DEFAULT_ADMIN_EMAIL;
        }
        String normalizedEmail = adminEmail.trim().toLowerCase();

        userRepository.findByEmail(normalizedEmail).ifPresentOrElse(
                user -> {
                    Set<Role> roles = user.getRoles();
                    if (roles == null || !roles.contains(Role.ADMIN)) {
                        if (roles == null) {
                            user.setRoles(EnumSet.of(Role.USER, Role.ADMIN));
                        } else {
                            roles.add(Role.ADMIN);
                            if (!roles.contains(Role.USER)) {
                                roles.add(Role.USER);
                            }
                            user.setRoles(roles);
                        }
                        userRepository.save(user);
                        log.info("AdminInitializer: {} kullanıcısına ADMIN rolü eklendi.", normalizedEmail);
                    }
                },
                () -> {
                    User admin = new User();
                    admin.setEmail(normalizedEmail);
                    admin.setRoles(EnumSet.of(Role.USER, Role.ADMIN));
                    admin.setEnabled(true);
                    admin.setEmailVerified(false);
                    planRepository.findByCode("FREE").ifPresent(admin::setSubscriptionPlan);
                    userRepository.save(admin);
                    log.info("AdminInitializer: Admin hesabı oluşturuldu: {} (giriş için e-posta kodu kullanın)", normalizedEmail);
                }
        );
    }
}
