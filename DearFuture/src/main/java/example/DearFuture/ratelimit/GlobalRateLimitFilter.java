package example.DearFuture.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Applies global per-IP rate limit (Redis) before any other filter.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class GlobalRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Value("${app.rate-limit.use-x-forwarded-for:false}")
    private boolean useXForwardedFor;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientIp = getClientIp(request);
        rateLimitService.checkGlobalLimit(clientIp);
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        if (useXForwardedFor) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }
}
