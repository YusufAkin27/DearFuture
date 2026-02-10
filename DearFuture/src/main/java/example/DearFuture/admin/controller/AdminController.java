package example.DearFuture.admin.controller;

import example.DearFuture.admin.dto.request.UpdatePlanRequest;
import example.DearFuture.admin.dto.response.AdminMessageResponse;
import example.DearFuture.admin.dto.response.DashboardStatsResponse;
import example.DearFuture.admin.dto.response.PlanDetailResponse;
import example.DearFuture.admin.service.AdminService;
import example.DearFuture.user.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin paneli REST API'leri.
 * Tüm endpoint'ler ADMIN rolüne sahip kullanıcılar tarafından erişilebilir.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ════════════════════════════════════════
    //  Dashboard
    // ════════════════════════════════════════

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ════════════════════════════════════════
    //  Kullanıcı Yönetimi
    // ════════════════════════════════════════

    /** Tüm kullanıcıları listele */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /** Tek kullanıcı detayı */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUser(userId));
    }

    /** Kullanıcı sil */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok("Kullanıcı silindi.");
    }

    /** Kullanıcıyı aktif/pasif yap */
    @PatchMapping("/users/{userId}/toggle-enabled")
    public ResponseEntity<String> toggleUserEnabled(
            @PathVariable Long userId,
            @RequestParam boolean enabled) {
        adminService.toggleUserEnabled(userId, enabled);
        return ResponseEntity.ok(enabled ? "Kullanıcı aktif edildi." : "Kullanıcı devre dışı bırakıldı.");
    }

    /** Kullanıcının planını değiştir */
    @PatchMapping("/users/{userId}/change-plan")
    public ResponseEntity<String> changeUserPlan(
            @PathVariable Long userId,
            @RequestParam String planCode) {
        adminService.changeUserPlan(userId, planCode);
        return ResponseEntity.ok("Plan değiştirildi: " + planCode);
    }

    // ════════════════════════════════════════
    //  Plan Yönetimi
    // ════════════════════════════════════════

    /** Tüm planları listele (kullanıcı sayılarıyla birlikte) */
    @GetMapping("/plans")
    public ResponseEntity<List<PlanDetailResponse>> getAllPlans() {
        return ResponseEntity.ok(adminService.getAllPlans());
    }

    /** Tek plan detayı */
    @GetMapping("/plans/{planId}")
    public ResponseEntity<PlanDetailResponse> getPlan(@PathVariable Long planId) {
        return ResponseEntity.ok(adminService.getPlan(planId));
    }

    /** Plan güncelle (fiyat, özellikler, limitler, açıklama vb.) */
    @PutMapping("/plans/{planId}")
    public ResponseEntity<PlanDetailResponse> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody UpdatePlanRequest request) {
        return ResponseEntity.ok(adminService.updatePlan(planId, request));
    }

    // ════════════════════════════════════════
    //  Mesaj Yönetimi
    // ════════════════════════════════════════

    /** Tüm mesajları listele */
    @GetMapping("/messages")
    public ResponseEntity<List<AdminMessageResponse>> getAllMessages() {
        return ResponseEntity.ok(adminService.getAllMessages());
    }

    /** Belirli kullanıcının mesajlarını listele */
    @GetMapping("/messages/user/{userId}")
    public ResponseEntity<List<AdminMessageResponse>> getMessagesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getMessagesByUser(userId));
    }

    /** Mesaj sil */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long messageId) {
        adminService.deleteMessage(messageId);
        return ResponseEntity.ok("Mesaj silindi.");
    }
}
