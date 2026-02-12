package example.DearFuture.contact_us.controller;

import example.DearFuture.contact_us.dto.ContactUsMessage;
import example.DearFuture.contact_us.dto.ResponseMessage;
import example.DearFuture.contact_us.service.ContactUsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * İletişim formu API. Giriş gerekmez.
 * Kullanıcı mesaj gönderir ve e-posta doğrulama kodu ile doğrular.
 */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ContactUsController {

    private final ContactUsService contactUsService;

    /**
     * Kullanıcı mesaj gönderir (giriş gerekmez).
     */
    @PostMapping("/send")
    public ResponseEntity<ResponseMessage> sendMessage(@Valid @RequestBody ContactUsMessage message) {
        ResponseMessage response = contactUsService.sendMessage(message);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * E-posta doğrulama kodu ile mesajı doğrular. Body: {"code":"123456"}
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ResponseMessage> verifyEmail(@RequestBody String verificationData) {
        if (verificationData == null || verificationData.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage("Doğrulama kodu boş olamaz.", false));
        }
        ResponseMessage response = contactUsService.verifyEmail(verificationData);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
