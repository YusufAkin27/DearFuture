package example.DearFuture.ratelimit;

import example.DearFuture.exception.security.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis-backed rate limiting using fixed-window (INCR + EXPIRE).
 * Keys: ratelimit:auth:send:{email}, ratelimit:auth:verify:{email}, ratelimit:ip:auth:{ip}, ratelimit:ip:global:{ip}.
 */
@Service
public class RateLimitService {

    private static final String PREFIX_AUTH_SEND = "ratelimit:auth:send:";
    private static final String PREFIX_AUTH_VERIFY = "ratelimit:auth:verify:";
    private static final String PREFIX_IP_AUTH = "ratelimit:ip:auth:";
    private static final String PREFIX_IP_GLOBAL = "ratelimit:ip:global:";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.rate-limit.auth.send-code.max:5}")
    private int sendCodeMax;

    @Value("${app.rate-limit.auth.send-code.window-minutes:15}")
    private int sendCodeWindowMinutes;

    @Value("${app.rate-limit.auth.verify.max:10}")
    private int verifyMax;

    @Value("${app.rate-limit.auth.verify.window-minutes:15}")
    private int verifyWindowMinutes;

    @Value("${app.rate-limit.global.per-minute:100}")
    private int globalPerMinute;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Enforces send-code / resend-code limit per email and per IP.
     * Call before sending a new login code.
     */
    public void checkSendCodeLimit(String email, String clientIp) {
        String emailKey = PREFIX_AUTH_SEND + normalizeKey(email);
        String ipKey = PREFIX_IP_AUTH + normalizeKey(clientIp);
        int windowSeconds = sendCodeWindowMinutes * 60;

        long emailCount = incrementAndExpire(emailKey, windowSeconds);
        if (emailCount > sendCodeMax) {
            throw new RateLimitExceededException(
                    "Too many code requests for this email. Try again in " + sendCodeWindowMinutes + " minutes.");
        }

        long ipCount = incrementAndExpire(ipKey, windowSeconds);
        if (ipCount > sendCodeMax) {
            throw new RateLimitExceededException(
                    "Too many requests from your IP. Try again in " + sendCodeWindowMinutes + " minutes.");
        }
    }

    /**
     * Enforces verify limit per email (and optionally IP) to prevent brute-force.
     */
    public void checkVerifyLimit(String email, String clientIp) {
        String emailKey = PREFIX_AUTH_VERIFY + normalizeKey(email);
        int windowSeconds = verifyWindowMinutes * 60;

        long count = incrementAndExpire(emailKey, windowSeconds);
        if (count > verifyMax) {
            throw new RateLimitExceededException(
                    "Too many verification attempts. Try again in " + verifyWindowMinutes + " minutes.");
        }
    }

    /**
     * Enforces global per-IP rate limit (e.g. 100 requests per minute).
     * Used by the global filter.
     */
    public void checkGlobalLimit(String clientIp) {
        String key = PREFIX_IP_GLOBAL + normalizeKey(clientIp);
        int windowSeconds = 60; // 1 minute

        long count = incrementAndExpire(key, windowSeconds);
        if (count > globalPerMinute) {
            throw new RateLimitExceededException("Too many requests. Please try again later.");
        }
    }

    /**
     * INCR key, set EXPIRE on first increment (when count becomes 1), return current count.
     */
    private long incrementAndExpire(String key, int windowSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            count = 1L;
        }
        if (count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return count;
    }

    private String normalizeKey(String part) {
        if (part == null) return "unknown";
        return part.trim().toLowerCase().replaceAll("[^a-z0-9@._-]", "_");
    }
}
