package example.DearFuture.user.controller;

import example.DearFuture.user.dto.request.UpdateProfileRequest;
import example.DearFuture.user.dto.request.UpdateSettingsRequest;
import example.DearFuture.user.dto.response.ProfileResponse;
import example.DearFuture.user.dto.response.UserResponse;
import example.DearFuture.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {

        Long userId = (Long) authentication.getPrincipal();
        String photoUrl = userService.getProfilePhoto(userId);
        if (photoUrl != null && !photoUrl.isEmpty()) {
            response.sendRedirect(photoUrl);
            return;
        }
        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NO_CONTENT);
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
