package example.DearFuture.contact_us.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.DearFuture.contact_us.dto.ContactUsMessage;
import example.DearFuture.contact_us.dto.DataResponseMessage;
import example.DearFuture.contact_us.dto.ResponseMessage;
import example.DearFuture.contact_us.entity.ContactUs;
import example.DearFuture.contact_us.entity.EmailVerificationCode;
import example.DearFuture.exception.contract.ResourceNotFoundException;
import example.DearFuture.contact_us.repository.ContactUsRepository;
import example.DearFuture.contact_us.repository.EmailVerificationRepository;
import example.DearFuture.mail.EmailMessage;
import example.DearFuture.mail.EmailQueue;
import example.DearFuture.mail.LoginCodeEmailTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class ContactUsManager implements ContactUsService {

    private final ContactUsRepository contactUsRepository;
    private final EmailVerificationRepository verificationRepository;
    private final EmailQueue emailQueue;
    private final ObjectMapper objectMapper;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    private static final int MAX_MESSAGES_PER_15_MIN = 3;
    private static final int MAX_MESSAGES_PER_HOUR = 5;
    private static final int MAX_MESSAGES_PER_DAY = 10;

    @Override
    @Transactional
    public ResponseMessage sendMessage(@Valid ContactUsMessage message) {
        try {
            ResponseMessage validationResult = validateMessage(message);
            if (!validationResult.isSuccess()) {
                return validationResult;
            }
            ResponseMessage spamCheck = checkSpamProtection(message.getEmail());
            if (!spamCheck.isSuccess()) {
                return spamCheck;
            }
            if (!isValidEmail(message.getEmail())) {
                return new ResponseMessage("Geçerli bir e-posta adresi giriniz.", false);
            }
            if (message.getPhone() != null && !message.getPhone().trim().isEmpty()) {
                if (!isValidPhone(message.getPhone())) {
                    return new ResponseMessage("Geçerli bir telefon numarası formatı giriniz.", false);
                }
            }

            String cleanedMessage = sanitizeMessage(message.getMessage());
            String cleanedSubject = sanitizeMessage(message.getSubject());
            String cleanedName = sanitizeName(message.getName());

            ContactUs contactUs = ContactUs.builder()
                    .name(cleanedName)
                    .email(message.getEmail().toLowerCase().trim())
                    .phone(message.getPhone() != null ? message.getPhone().trim() : null)
                    .subject(cleanedSubject)
                    .message(cleanedMessage)
                    .verified(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            contactUsRepository.save(contactUs);
            log.info("İletişim mesajı kaydedildi: ID={}, Email={}", contactUs.getId(), contactUs.getEmail());

            String verificationCode = generateVerificationCode();
            invalidateOldCodes(message.getEmail());

            EmailVerificationCode code = new EmailVerificationCode();
            code.setEmail(message.getEmail().toLowerCase().trim());
            code.setCode(verificationCode);
            code.setUsed(false);
            code.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            code.setCreatedAt(LocalDateTime.now());
            verificationRepository.save(code);

            try {
                String emailBody = LoginCodeEmailTemplate.build(verificationCode, 15);
                EmailMessage emailMessage = EmailMessage.builder()
                        .toEmail(message.getEmail().toLowerCase().trim())
                        .subject("İletişim Doğrulama Kodu - Dear Future")
                        .body(emailBody)
                        .isHtml(true)
                        .build();
                emailQueue.enqueue(emailMessage);
                log.info("Doğrulama kodu e-postası kuyruğa eklendi: {}", message.getEmail());
            } catch (Exception e) {
                log.error("E-posta kuyruğa eklenirken hata: {}", e.getMessage());
                return new ResponseMessage(
                        "Mesajınız kaydedildi ancak doğrulama e-postası gönderilemedi. Lütfen daha sonra tekrar deneyin.",
                        false
                );
            }

            return new ResponseMessage(
                    "Mesajınız alındı. E-posta adresinize gönderilen 6 haneli doğrulama kodunu girin. (Kod 15 dakika geçerlidir)",
                    true
            );
        } catch (Exception e) {
            log.error("Mesaj gönderilirken hata: ", e);
            return new ResponseMessage("Mesaj gönderilemedi. Lütfen daha sonra tekrar deneyin.", false);
        }
    }

    private ResponseMessage validateMessage(ContactUsMessage message) {
        if (message == null) {
            return new ResponseMessage("Mesaj bilgileri boş olamaz.", false);
        }
        if (message.getName() == null || message.getName().trim().isEmpty()) {
            return new ResponseMessage("Ad Soyad alanı zorunludur.", false);
        }
        if (message.getName().trim().length() < 2 || message.getName().trim().length() > 200) {
            return new ResponseMessage("Ad Soyad 2-200 karakter olmalıdır.", false);
        }
        if (message.getEmail() == null || message.getEmail().trim().isEmpty()) {
            return new ResponseMessage("E-posta adresi zorunludur.", false);
        }
        if (message.getSubject() == null || message.getSubject().trim().isEmpty()) {
            return new ResponseMessage("Konu alanı zorunludur.", false);
        }
        if (message.getSubject().trim().length() < 3 || message.getSubject().trim().length() > 500) {
            return new ResponseMessage("Konu 3-500 karakter olmalıdır.", false);
        }
        if (message.getMessage() == null || message.getMessage().trim().isEmpty()) {
            return new ResponseMessage("Mesaj alanı zorunludur.", false);
        }
        if (message.getMessage().trim().length() < 10 || message.getMessage().trim().length() > 5000) {
            return new ResponseMessage("Mesaj 10-5000 karakter olmalıdır.", false);
        }
        return new ResponseMessage("OK", true);
    }

    private ResponseMessage checkSpamProtection(String email) {
        LocalDateTime now = LocalDateTime.now();
        String normalizedEmail = email.toLowerCase().trim();

        if (contactUsRepository.countByEmailAndCreatedAtAfter(normalizedEmail, now.minusMinutes(15)) >= MAX_MESSAGES_PER_15_MIN) {
            return new ResponseMessage("Çok fazla mesaj gönderdiniz. Lütfen 15 dakika sonra tekrar deneyin.", false);
        }
        if (contactUsRepository.countByEmailAndCreatedAtAfter(normalizedEmail, now.minusHours(1)) >= MAX_MESSAGES_PER_HOUR) {
            return new ResponseMessage("Saatlik mesaj limitine ulaştınız. Lütfen 1 saat sonra tekrar deneyin.", false);
        }
        if (contactUsRepository.countByEmailAndCreatedAtAfter(normalizedEmail, now.minusDays(1)) >= MAX_MESSAGES_PER_DAY) {
            return new ResponseMessage("Günlük mesaj limitine ulaştınız. Lütfen yarın tekrar deneyin.", false);
        }
        return new ResponseMessage("OK", true);
    }

    private boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty()
                && EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return true;
        String cleaned = phone.trim().replaceAll("[\\s\\-()]", "");
        return cleaned.length() >= 10 && cleaned.length() <= 15;
    }

    private String sanitizeMessage(String text) {
        if (text == null) return "";
        return text.trim()
                .replaceAll("<script[^>]*>.*?</script>", "")
                .replaceAll("<[^>]+>", "")
                .replaceAll("javascript:", "")
                .replaceAll("on\\w+=", "");
    }

    private String sanitizeName(String name) {
        if (name == null) return "";
        return name.trim().replaceAll("<[^>]+>", "").replaceAll("[<>\"'&]", "");
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void invalidateOldCodes(String email) {
        List<EmailVerificationCode> oldCodes = verificationRepository.findByEmailIgnoreCaseAndUsedFalse(email)
                .stream()
                .filter(c -> c.getExpiresAt().isAfter(LocalDateTime.now()))
                .toList();
        for (EmailVerificationCode code : oldCodes) {
            code.setUsed(true);
            verificationRepository.save(code);
        }
    }

    @Override
    @Transactional
    public ResponseMessage verifyEmail(String verificationData) {
        try {
            if (verificationData == null || verificationData.trim().isEmpty()) {
                return new ResponseMessage("Doğrulama kodu boş olamaz.", false);
            }
            String code;
            try {
                var data = objectMapper.readTree(verificationData);
                if (!data.has("code")) {
                    return new ResponseMessage("Doğrulama kodu bulunamadı.", false);
                }
                code = data.get("code").asText().trim();
            } catch (Exception e) {
                return new ResponseMessage("Geçersiz veri formatı.", false);
            }
            if (code.length() != 6 || !code.matches("^[0-9]{6}$")) {
                return new ResponseMessage("Doğrulama kodu 6 haneli sayı olmalıdır.", false);
            }

            var verificationOpt = verificationRepository.findTopByCodeAndUsedFalseOrderByCreatedAtDesc(code);
            if (verificationOpt.isEmpty()) {
                return new ResponseMessage("Geçersiz doğrulama kodu.", false);
            }
            EmailVerificationCode verification = verificationOpt.get();
            if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
                verification.setUsed(true);
                verificationRepository.save(verification);
                return new ResponseMessage("Doğrulama kodunun süresi dolmuş. Lütfen yeni kod isteyin.", false);
            }

            verification.setUsed(true);
            verificationRepository.save(verification);
            String email = verification.getEmail().toLowerCase().trim();

            var contactOpt = contactUsRepository.findFirstByEmailIgnoreCaseAndVerifiedFalseOrderByCreatedAtDesc(email);
            if (contactOpt.isEmpty()) {
                return new ResponseMessage("Bu kod için doğrulanacak mesaj bulunamadı.", false);
            }
            ContactUs contact = contactOpt.get();
            contact.setVerified(true);
            contactUsRepository.save(contact);
            log.info("İletişim mesajı doğrulandı: ID={}", contact.getId());

            return new ResponseMessage(
                    "E-posta adresiniz doğrulandı. Mesajınız alındı, teşekkür ederiz.",
                    true
            );
        } catch (Exception e) {
            log.error("Doğrulama hatası: ", e);
            return new ResponseMessage("Doğrulama işlemi başarısız. Lütfen daha sonra tekrar deneyin.", false);
        }
    }

    @Override
    public ResponseMessage getAllMessages() {
        try {
            List<ContactUs> messages = contactUsRepository.findByVerifiedTrue().stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList();
            return new DataResponseMessage<>(
                    messages.size() + " mesaj getirildi",
                    true,
                    messages
            );
        } catch (Exception e) {
            log.error("Mesajlar getirilirken hata: ", e);
            return new ResponseMessage("Mesajlar getirilemedi.", false);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactUs> getById(Long id) {
        return contactUsRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteMessage(Long id) {
        ContactUs contact = contactUsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("İletişim mesajı bulunamadı: " + id));
        contactUsRepository.delete(contact);
        log.info("İletişim mesajı silindi: id={}", id);
    }
}
