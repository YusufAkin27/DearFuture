package example.DearFuture.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Redis olmadan çalışma: rate limit kontrolü yapmaz.
 * app.rate-limit.enabled=false veya belirtilmediğinde kullanılır.
 */
@Service
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpRateLimitService implements RateLimitService {

    @Override
    public void checkSendCodeLimit(String email, String clientIp) {
        // no-op
    }

    @Override
    public void checkVerifyLimit(String email, String clientIp) {
        // no-op
    }

    @Override
    public void checkGlobalLimit(String clientIp) {
        // no-op
    }
}
