package example.DearFuture.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 giriş hatası (authorization_request_not_found, invalid_credentials vb.):
 * Backend /login?error yerine frontend /login?error=... ile yönlendirir.
 */
@Component
@Slf4j
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend-url:https://dearfuture.com.tr}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn("OAuth2 login failure: {}", exception.getMessage());
        String message = "Giriş başarısız. Lütfen giriş sayfasından tekrar Google ile giriş yapın.";
        if (exception.getMessage() != null && exception.getMessage().contains("authorization_request_not_found")) {
            message = "Oturum süresi doldu veya geçersiz istek. Lütfen tekrar 'Google ile Giriş Yap' butonuna tıklayın.";
        }
        String base = frontendUrl != null && !frontendUrl.isBlank()
                ? frontendUrl.trim().replaceAll("/$", "")
                : "https://dearfuture.com.tr";
        String redirectUrl = base + "/login?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
