package example.DearFuture.auth.handler;

import example.DearFuture.auth.jwt.JwtUtil;
import example.DearFuture.auth.service.CustomOAuth2UserService;
import example.DearFuture.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Google OAuth2 başarılı giriş sonrası: JWT üretir ve frontend'e token ile yönlendirir.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final CustomOAuth2UserService oauth2UserService;
    private final JwtUtil jwtUtil;

    @Value("${app.frontend-url:https://dearfuture.com.tr}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            redirectWithError(request, response, "E-posta alınamadı.");
            return;
        }

        User user = oauth2UserService.getOrCreateUserFromOAuth2(oauth2User);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRoles());
        String redirectUrl = buildRedirectUrl(token, null);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        String redirectUrl = buildRedirectUrl(null, message);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String buildRedirectUrl(String token, String errorMessage) {
        String base = frontendUrl != null && !frontendUrl.isBlank()
                ? frontendUrl.trim().replaceAll("/$", "")
                : "https://dearfuture.com.tr";
        String path = "/auth/callback";
        if (token != null) {
            return base + path + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        }
        if (errorMessage != null) {
            return base + path + "?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        }
        return base + path;
    }
}
