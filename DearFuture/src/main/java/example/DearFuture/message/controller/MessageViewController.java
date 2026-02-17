package example.DearFuture.message.controller;

import example.DearFuture.message.dto.response.MessageViewResponse;
import example.DearFuture.message.encryption.MessageEncryptionService;
import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.repository.FutureMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Alıcının mesajı görüntülemesi için public (kimlik doğrulama gerektirmeyen) endpoint.
 * Mesaj token'ı ile erişilir. Mesaj zamanı gelmemişse erişim reddedilir.
 */
@RestController
@RequestMapping("/api/messages/view")
@RequiredArgsConstructor
public class MessageViewController {

    private final FutureMessageRepository futureMessageRepository;
    private final MessageEncryptionService messageEncryptionService;

    /**
     * Token ile mesajı görüntüle.
     * Mesajın zamanlanmış tarihi (scheduledAt) geçmemişse 403 döner.
     * Token geçersizse 404 döner.
     */
    @GetMapping("/{token}")
    public ResponseEntity<?> viewMessage(@PathVariable String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Geçersiz token.");
        }

        FutureMessage message = futureMessageRepository.findByViewTokenWithContents(token).orElse(null);

        if (message == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Mesaj bulunamadı.");
        }

        // Mesajın zamanı gelmemişse erişimi reddet
        if (message.getScheduledAt().isAfter(Instant.now())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Bu mesaj henüz açılmadı. Zamanı geldiğinde görüntüleyebilirsiniz.");
        }

        // Fotoğraf, video, ses ve dosya URL'lerini şifreden çöz (istemci gerçek Cloudinary linkini görsün)
        messageEncryptionService.decryptMessageFileUrls(message);
        return ResponseEntity.ok(MessageViewResponse.fromEntity(message));
    }
}
