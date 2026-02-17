package example.DearFuture.message.encryption;

import example.DearFuture.message.entity.ContentType;
import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.FutureMessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Mesaj metin içeriğini AES-256-GCM ile şifreler.
 * Kayıt sırasında şifrelenir; zamanı geldiğinde scheduler tarafından çözülür.
 */
@Slf4j
@Service
public class MessageEncryptionService {

    private static final String PREFIX = "ENCv1:";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;

    private final SecretKey key;
    private final boolean enabled;

    public MessageEncryptionService(
            @Value("${app.message-encryption-key:}") String keyBase64) {
        this.enabled = keyBase64 != null && !keyBase64.isBlank();
        if (enabled) {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(keyBase64.trim());
                if (keyBytes.length != 32) {
                    throw new IllegalArgumentException("app.message-encryption-key must be 32 bytes (base64 decoded)");
                }
                this.key = new SecretKeySpec(keyBytes, "AES");
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid app.message-encryption-key: " + e.getMessage());
            }
        } else {
            this.key = null;
            log.warn("Message encryption disabled: app.message-encryption-key not set. Set a 32-byte base64 key for production.");
        }
    }

    /**
     * Metni şifreler. Boş veya null ise olduğu gibi döner.
     * Çıktı formatı: ENCv1: + base64(IV + ciphertext + tag)
     */
    public String encrypt(String plaintext) {
        if (!enabled || key == null || plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] plain = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plain);
            ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);
            return PREFIX + Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            log.error("Message encryption failed", e);
            throw new RuntimeException("Şifreleme yapılamadı.", e);
        }
    }

    /**
     * ENCv1: ile başlayan metni çözer. Değilse olduğu gibi döner.
     */
    public String decrypt(String encrypted) {
        if (encrypted == null || !encrypted.startsWith(PREFIX)) {
            return encrypted;
        }
        if (!enabled || key == null) {
            log.warn("Decryption requested but encryption is disabled");
            return encrypted;
        }
        try {
            String b64 = encrypted.substring(PREFIX.length());
            byte[] raw = Base64.getDecoder().decode(b64);
            ByteBuffer buf = ByteBuffer.wrap(raw);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            buf.get(iv);
            byte[] ciphertext = new byte[buf.remaining()];
            buf.get(ciphertext);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] plain = cipher.doFinal(ciphertext);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Message decryption failed", e);
            throw new RuntimeException("Şifre çözülemedi.", e);
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Mesajın tüm TEXT içeriklerini şifreden çözer (yerinde günceller).
     * Scheduler iletim öncesi bu metodu çağırır; ardından mesaj kaydedilir.
     */
    public void decryptMessageContents(FutureMessage message) {
        if (message == null || message.getContents() == null) return;
        for (FutureMessageContent content : message.getContents()) {
            if (content.getType() == ContentType.TEXT && content.getTextContent() != null
                    && isEncrypted(content.getTextContent())) {
                content.setTextContent(decrypt(content.getTextContent()));
            }
        }
    }
}
