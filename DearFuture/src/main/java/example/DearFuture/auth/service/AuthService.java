package example.DearFuture.auth.service;

import example.DearFuture.auth.entity.LoginCode;
import example.DearFuture.auth.jwt.JwtUtil;
import example.DearFuture.auth.repository.LoginCodeRepository;
import example.DearFuture.auth.response.AuthResponse;
import example.DearFuture.exception.security.CodeAlreadyUsedException;
import example.DearFuture.exception.security.CodeExpiredException;
import example.DearFuture.exception.security.InvalidCodeException;
import example.DearFuture.mail.EmailMessage;
import example.DearFuture.mail.LoginCodeEmailTemplate;
import example.DearFuture.mail.MailService;
import example.DearFuture.ratelimit.RateLimitService;
import example.DearFuture.user.entity.Role;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final String GOOGLE_TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final UserRepository userRepository;
    private final LoginCodeRepository loginCodeRepository;
    private final JwtUtil jwtUtil;
    private final MailService mailService;
    private final RateLimitService rateLimitService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final RestTemplate restTemplate;


    /**
     * Kullanıcıya giriş kodunu oluşturur ve mail kuyruğuna ekler (HTML).
     * Rate limiting: Redis ile e-posta ve IP bazlı (RateLimitService).
     */
    public void sendLoginCode(String email, String clientIp) {
        rateLimitService.checkSendCodeLimit(email, clientIp);

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setRoles(EnumSet.of(Role.USER));
            userRepository.save(newUser);
        }

        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        LoginCode loginCode = new LoginCode();
        loginCode.setEmail(email);
        loginCode.setCode(code);
        loginCodeRepository.save(loginCode);

        String htmlBody = LoginCodeEmailTemplate.build(code, CODE_EXPIRY_MINUTES);
        EmailMessage emailMessage = EmailMessage.builder()
                .toEmail(email)
                .subject("Dear Future - Giriş Kodunuz")
                .body(htmlBody)
                .isHtml(true)
                .build();
        mailService.enqueueEmail(emailMessage);

        log.info("Login kodu oluşturuldu ve kuyruğa eklendi: {}", email);
    }

    /**
     * Kullanıcının gönderilen kodu doğrulamasını yapar ve JWT üretir.
     * Sadece EN SON gönderilen kod geçerlidir.
     */
    public AuthResponse verifyCodeAndLogin(String email, String code, String clientIp) {
        rateLimitService.checkVerifyLimit(email, clientIp);

        LoginCode loginCode = loginCodeRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new InvalidCodeException("No login code found for this email."));

        if (!loginCode.getCode().equals(code)) {
            throw new InvalidCodeException("Invalid verification code.");
        }

        if (loginCode.isUsed()) {
            throw new CodeAlreadyUsedException("This code has already been used.");
        }

        if (loginCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CodeExpiredException("Verification code has expired.");
        }

        loginCode.setUsed(true);
        loginCodeRepository.save(loginCode);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after verification? This should not happen."));

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRoles()
        );

        log.info("Kullanıcı giriş yaptı: {}", email);

        return new AuthResponse(token);
    }

    /**
     * Mobil uygulama: Google ID token ile giriş. Token Google tokeninfo ile doğrulanır,
     * kullanıcı bulunur/oluşturulur ve JWT döndürülür.
     */
    public AuthResponse loginWithGoogleIdToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Google ID token is required");
        }
        Map<String, Object> payload;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(GOOGLE_TOKENINFO_URL + idToken, Map.class);
            if (response == null || response.containsKey("error")) {
                throw new InvalidCodeException("Geçersiz Google token.");
            }
            payload = response;
        } catch (RestClientException e) {
            log.warn("Google tokeninfo failed: {}", e.getMessage());
            throw new InvalidCodeException("Google ile doğrulama başarısız.");
        }
        String email = (String) payload.get("email");
        if (email == null || email.isBlank()) {
            throw new InvalidCodeException("Google hesabından e-posta alınamadı.");
        }
        // aud: web, Android ve iOS client ID'leri farklı olabilir; token zaten Google tarafından doğrulandı
        String aud = (String) payload.get("aud");
        if (aud == null || aud.isBlank()) {
            throw new InvalidCodeException("Geçersiz Google token.");
        }
        String givenName = (String) payload.get("given_name");
        String familyName = (String) payload.get("family_name");
        String fullName = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        User user = customOAuth2UserService.getOrCreateUserFromGoogle(email, givenName, familyName, fullName, picture);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRoles());
        log.info("Google (mobil) ile giriş: {}", email);
        return new AuthResponse(token);
    }
}
