package example.DearFuture.user.controller;

import example.DearFuture.message.service.FutureMessageService;
import example.DearFuture.user.dto.request.UpdateProfileRequest;
import example.DearFuture.user.dto.request.UpdateSettingsRequest;
import example.DearFuture.user.dto.response.MessageQuotaResponse;
import example.DearFuture.user.dto.response.ProfileResponse;
import example.DearFuture.user.dto.response.UsageResponse;
import example.DearFuture.user.dto.response.UserResponse;
import example.DearFuture.user.service.UsageService;
import example.DearFuture.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UsageService usageService;
    private final FutureMessageService futureMessageService;

    /* ===================== PROFILE ===================== */

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        ProfileResponse response =
                userService.updateProfile(userId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                userService.getProfile(userId)
        );
    }

    /** Bu dönem kalan mesaj kullanım hakkı (abonelik planına göre). */
    @GetMapping("/usage")
    public ResponseEntity<UsageResponse> getUsage(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(usageService.getUsage(userId));
    }

    /**
     * Kullanıcı alanları: bu ay/dönem toplam atılan mesaj (bekleyen+iletilen),
     * kalan hak, plan bilgisi (PLUS/PREMIUM). Hesaplama abonelik dönemine göre yapılır.
     */
    @GetMapping("/fields")
    public ResponseEntity<UsageResponse> getFields(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(usageService.getUsage(userId));
    }

    /**
     * Planına göre mesaj kotası: toplam gönderilen mesaj sayısı ve kalan mesaj hakkı (long).
     * FREE: bekleyen+iletilen toplam; PLUS/PREMIUM: dönem içi kullanım.
     */
    @GetMapping("/message-quota")
    public ResponseEntity<MessageQuotaResponse> getMessageQuota(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(futureMessageService.getMessageQuota(userId));
    }

    /* ===================== SETTINGS ===================== */

    @PutMapping("/settings")
    public ResponseEntity<UserResponse> updateSettings(
            @Valid @RequestBody UpdateSettingsRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                userService.updateSettings(userId, request)
        );
    }

    /* ===================== PROFILE PHOTO ===================== */

    @PostMapping("/profile/photo")
    public ResponseEntity<Void> uploadProfilePhoto(
            @RequestParam("photo") MultipartFile file,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        userService.uploadProfilePhoto(userId, file);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/profile/photo")
    public ResponseEntity<Void> deleteProfilePhoto(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        userService.deleteProfilePhoto(userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile/photo")
    public void getProfilePhoto(
            Authentication authentication,
            HttpServletResponse response) throws IOException {

        Long userId = (Long) authentication.getPrincipal();
        String photoUrl = userService.getProfilePhoto(userId);
        if (photoUrl != null && !photoUrl.isEmpty()) {
            response.sendRedirect(photoUrl);
            return;
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /* ===================== ACCOUNT ===================== */

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        userService.deleteAccount(userId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/account/deactivate")
    public ResponseEntity<Void> deactivateAccount(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        userService.deactivateAccount(userId);

        return ResponseEntity.noContent().build();
    }


}
