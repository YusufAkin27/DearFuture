package example.DearFuture.message.controller;

import example.DearFuture.message.dto.response.PublicMessageItemResponse;
import example.DearFuture.message.service.PublicMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Herkese açık mesajlar sayfası API.
 * - Liste: giriş gerekmez; giriş yapılmışsa "yıldızlı mı" bilgisi döner.
 * - Yıldızla / yıldızı kaldır / yıldızlılarım: giriş gerekir.
 */
@RestController
@RequestMapping("/api/messages/public")
@RequiredArgsConstructor
public class PublicMessageController {

    private final PublicMessageService publicMessageService;

    /**
     * Açılmış ve herkese açık mesajları listeler. Giriş gerekmez.
     * Giriş yapılmışsa her mesaj için starredByMe alanı dolu olur.
     */
    @GetMapping
    public ResponseEntity<List<PublicMessageItemResponse>> listPublicMessages() {
        Long currentUserId = getCurrentUserIdOrNull();
        List<PublicMessageItemResponse> list = publicMessageService.getPublicMessages(currentUserId);
        return ResponseEntity.ok(list);
    }

    /**
     * Giriş yapan kullanıcının yıldızladığı mesajları listeler. Giriş gerekir.
     */
    @GetMapping("/starred")
    public ResponseEntity<List<PublicMessageItemResponse>> getMyStarred() {
        List<PublicMessageItemResponse> list = publicMessageService.getMyStarredMessages();
        return ResponseEntity.ok(list);
    }

    /**
     * Mesajı yıldızla. Giriş gerekir.
     */
    @PostMapping("/{messageId}/star")
    public ResponseEntity<Void> star(@PathVariable Long messageId) {
        publicMessageService.starMessage(messageId);
        return ResponseEntity.ok().build();
    }

    /**
     * Yıldızı kaldır. Giriş gerekir.
     */
    @DeleteMapping("/{messageId}/star")
    public ResponseEntity<Void> unstar(@PathVariable Long messageId) {
        publicMessageService.unstarMessage(messageId);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof Long)) {
            return null;
        }
        return (Long) auth.getPrincipal();
    }
}
