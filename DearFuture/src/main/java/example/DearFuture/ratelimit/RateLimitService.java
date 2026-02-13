package example.DearFuture.ratelimit;

/**
 * Rate limiting: e-posta / IP bazlı limit kontrolü.
 * Redis yoksa veya app.rate-limit.enabled=false ise NoOpRateLimitService kullanılır (kontrol yapılmaz).
 */
public interface RateLimitService {

    void checkSendCodeLimit(String email, String clientIp);

    void checkVerifyLimit(String email, String clientIp);

    void checkGlobalLimit(String clientIp);
}
