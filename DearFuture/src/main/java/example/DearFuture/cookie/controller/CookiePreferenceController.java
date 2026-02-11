package example.DearFuture.cookie.controller;

import example.DearFuture.cookie.dto.CookiePreferenceRequest;
import example.DearFuture.cookie.entity.CookiePreference;
import example.DearFuture.cookie.service.CookiePreferenceService;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Cookie tercihleri controller.
 * GDPR uyumlu çerez yönetimi API'leri.
 */
@RestController
@RequestMapping("/api/cookies")
@RequiredArgsConstructor
@Validated
public class CookiePreferenceController {

    private final CookiePreferenceService cookiePreferenceService;
    private final UserRepository userRepository;

    @PostMapping("/preferences")
    public ResponseEntity<Map<String, Object>> saveCookiePreference(
            @Valid @RequestBody CookiePreferenceRequest request,
            @AuthenticationPrincipal Object principal,
            HttpServletRequest httpRequest) {
        if (request.getNecessary() != null && !request.getNecessary()) {
            request.setNecessary(true);
        }
        Long userId = principal != null ? (Long) principal : null;
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        try {
            CookiePreference preference = cookiePreferenceService.saveCookiePreference(request, user, httpRequest);
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Çerez tercihleriniz başarıyla kaydedildi.");
            body.put("data", buildPreferenceResponse(preference));
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Geçersiz istek: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Çerez tercihleri kaydedilirken bir hata oluştu."));
        }
    }

    @GetMapping("/preferences")
    public ResponseEntity<Map<String, Object>> getCookiePreference(
            @RequestParam(required = false) String sessionId,
            @AuthenticationPrincipal Object principal,
            HttpServletRequest httpRequest) {
        Long userId = principal != null ? (Long) principal : null;
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        try {
            var preferenceOpt = cookiePreferenceService.getCookiePreference(user, sessionId, httpRequest);
            Map<String, Object> body = new HashMap<>();
            if (preferenceOpt.isPresent()) {
                body.put("message", "Çerez tercihleri getirildi");
                Map<String, Object> data = buildPreferenceResponse(preferenceOpt.get());
                data.put("consentVersionCurrent", cookiePreferenceService.isConsentVersionCurrent(user, sessionId, httpRequest));
                body.put("data", data);
            } else {
                body.put("message", "Çerez tercihi bulunamadı. Lütfen tercihlerinizi belirleyin.");
                body.put("data", buildDefaultPreferenceResponse());
            }
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Çerez tercihleri getirilirken bir hata oluştu."));
        }
    }

    @DeleteMapping("/preferences")
    public ResponseEntity<Map<String, Object>> revokeConsent(
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String sessionId,
            @AuthenticationPrincipal Object principal,
            HttpServletRequest httpRequest) {
        Long userId = principal != null ? (Long) principal : null;
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        try {
            CookiePreference preference = cookiePreferenceService.revokeConsent(user, sessionId, reason, httpRequest);
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Çerez tercihleriniz iptal edildi.");
            body.put("data", buildPreferenceResponse(preference));
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "İptal edilecek tercih bulunamadı: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Consent iptal edilirken bir hata oluştu."));
        }
    }

    @GetMapping("/preferences/check-version")
    public ResponseEntity<Map<String, Object>> checkConsentVersion(
            @RequestParam(required = false) String sessionId,
            @AuthenticationPrincipal Object principal,
            HttpServletRequest httpRequest) {
        Long userId = principal != null ? (Long) principal : null;
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        try {
            boolean isCurrent = cookiePreferenceService.isConsentVersionCurrent(user, sessionId, httpRequest);
            Map<String, Object> data = new HashMap<>();
            data.put("isCurrent", isCurrent);
            data.put("currentVersion", "1.0");
            data.put("message", isCurrent ? "Cookie tercihleriniz güncel." : "Cookie politikası güncellendi. Lütfen yeni tercihlerinizi belirleyin.");
            return ResponseEntity.ok(Map.of("message", "Versiyon kontrolü tamamlandı", "data", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Versiyon kontrolü yapılırken bir hata oluştu."));
        }
    }

    private Map<String, Object> buildPreferenceResponse(CookiePreference preference) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", preference.getId());
        data.put("necessary", preference.getNecessary());
        data.put("analytics", preference.getAnalytics());
        data.put("marketing", preference.getMarketing());
        data.put("personalization", preference.getPersonalization());
        data.put("consentGiven", preference.getConsentGiven());
        data.put("consentDate", preference.getConsentDate());
        data.put("consentVersion", preference.getConsentVersion());
        data.put("updatedAt", preference.getUpdatedAt());
        data.put("revokedAt", preference.getRevokedAt());
        data.put("isConsentValid", preference.isConsentValid());
        return data;
    }

    private Map<String, Object> buildDefaultPreferenceResponse() {
        Map<String, Object> data = new HashMap<>();
        data.put("necessary", true);
        data.put("analytics", false);
        data.put("marketing", false);
        data.put("personalization", false);
        data.put("consentGiven", false);
        data.put("consentVersion", "1.0");
        data.put("consentVersionCurrent", false);
        return data;
    }
}
