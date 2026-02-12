package example.DearFuture.contact_us.service;

import example.DearFuture.contact_us.dto.ContactUsMessage;
import example.DearFuture.contact_us.dto.ResponseMessage;
import example.DearFuture.contact_us.entity.ContactUs;
import jakarta.validation.Valid;

import java.util.Optional;

/**
 * Contact Us service interface.
 * Kullanıcı mesaj gönderir, admin sadece mesajları görüntüler (yanıt yok).
 */
public interface ContactUsService {

    /**
     * Mesaj gönder (giriş gerekmez).
     */
    ResponseMessage sendMessage(@Valid ContactUsMessage message);

    /**
     * E-posta doğrulama kodu ile mesajı doğrula.
     */
    ResponseMessage verifyEmail(String verificationData);

    /**
     * Tüm doğrulanmış mesajları getir (sadece admin).
     */
    ResponseMessage getAllMessages();

    /**
     * Id ile tek iletişim mesajı getir (admin detay için).
     */
    Optional<ContactUs> getById(Long id);

    /**
     * İletişim mesajı sil (admin).
     */
    void deleteMessage(Long id);
}
