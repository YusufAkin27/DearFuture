package example.DearFuture.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Redis sadece rate limit açıkken yüklenir. app.rate-limit.enabled=true ise Redis bağlantısı kurulur.
 */
@Configuration
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true")
@Import(RedisAutoConfiguration.class)
public class RedisRateLimitConfig {
}
