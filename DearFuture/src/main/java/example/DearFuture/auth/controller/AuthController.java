package example.DearFuture.auth.controller;

import example.DearFuture.auth.request.SendCodeRequest;
import example.DearFuture.auth.request.VerifyCodeRequest;
import example.DearFuture.auth.response.AuthResponse;
import example.DearFuture.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.rate-limit.use-x-forwarded-for}")
    private boolean useXForwardedFor;

    private String getClientIp(HttpServletRequest request) {
        if (useXForwardedFor && request != null) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
        }
        return request != null && request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestBody SendCodeRequest request, HttpServletRequest httpRequest) {
        authService.sendLoginCode(request.getEmail(), getClientIp(httpRequest));
        return ResponseEntity.ok("doğrulama kodu gönderildi");
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verifyCode(@RequestBody VerifyCodeRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.verifyCodeAndLogin(
                request.getEmail(),
                request.getCode(),
                getClientIp(httpRequest)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-code")
    public ResponseEntity<String> resendCode(@RequestBody SendCodeRequest request, HttpServletRequest httpRequest) {
        authService.sendLoginCode(request.getEmail(), getClientIp(httpRequest));
        return ResponseEntity.ok("doğrulama kodu gönderildi");
    }
}
