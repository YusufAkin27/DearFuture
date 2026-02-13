package example.DearFuture.user.entity;

import example.DearFuture.message.entity.ContentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Abonelik planı entity'si. Admin tarafından yönetilir.
 * Plan özellikleri (fiyat, limitler, açıklama vb.) veritabanında saklanır.
 */
@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Plan kodu: FREE, PLUS, PREMIUM vb. Benzersiz tanımlayıcı. */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** Plan adı (Türkçe): Ücretsiz, Plus, Premium */
    @Column(nullable = false, length = 100)
    private String name;

    /** Plan açıklaması */
    @Column(length = 500)
    private String description;

    /** Aylık fiyat (TL). FREE için 0. */
    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    /** Fiyat etiketi: "₺/ay" */
    @Column(name = "price_label", length = 20)
    private String priceLabel;

    // ── Limitler ──

    /** Maksimum aktif (zamanlanmış) mesaj sayısı */
    @Column(name = "max_messages", nullable = false)
    private int maxMessages;

    /** Mesaj başına maksimum alıcı sayısı */
    @Column(name = "max_recipients_per_message", nullable = false)
    private int maxRecipientsPerMessage;

    /** Fotoğraf ekleme izni */
    @Column(name = "allow_photo", nullable = false)
    private boolean allowPhoto;

    /** Dosya ekleme izni */
    @Column(name = "allow_file", nullable = false)
    private boolean allowFile;

    /** Ses kaydı ekleme izni */
    @Column(name = "allow_voice", nullable = false)
    private boolean allowVoice;

    /** Mesaj başına maksimum fotoğraf sayısı */
    @Column(name = "max_photos_per_message", nullable = false)
    private int maxPhotosPerMessage;

    /** Fotoğraf başına maksimum boyut (byte) */
    @Column(name = "max_photo_size_bytes", nullable = false)
    private long maxPhotoSizeBytes;

    /** Mesaj başına maksimum dosya sayısı */
    @Column(name = "max_files_per_message", nullable = false)
    private int maxFilesPerMessage;

    /** Dosya başına maksimum boyut (byte) */
    @Column(name = "max_file_size_bytes", nullable = false)
    private long maxFileSizeBytes;

    /** Mesaj başına maksimum ses kaydı sayısı (allowVoice true ise kullanılır) */
    @Column(name = "max_audio_per_message", nullable = false)
    private int maxAudioPerMessage;

    /** Ses kaydı başına maksimum boyut (byte). 0 ise maxFileSizeBytes kullanılır. */
    @Column(name = "max_audio_size_bytes", nullable = false)
    private long maxAudioSizeBytes;

    // ── UI / Admin ──

    /** Özellik listesi (fiyatlandırma sayfasında gösterilir) */
    @ElementCollection
    @CollectionTable(name = "subscription_plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature", length = 255)
    @OrderColumn(name = "feature_order")
    private List<String> features;

    /** Öne çıkarılan plan mı (fiyatlandırma sayfasında vurgulanır) */
    @Column(nullable = false)
    private boolean recommended;

    /** Aktif mi (false ise yeni abonelik satın alınamaz) */
    @Column(nullable = false)
    private boolean active;

    /** Görüntüleme sırası (fiyatlandırma sayfasında) */
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    // ── Yardımcı metodlar ──

    /** Bu plan ücretsiz mi? */
    public boolean isFree() {
        return monthlyPrice == null || monthlyPrice.compareTo(BigDecimal.ZERO) == 0;
    }

    /** Bu planın kullanabileceği içerik tiplerini döner. */
    public Set<ContentType> getAllowedContentTypes() {
        if (allowVoice) {
            return Set.of(ContentType.TEXT, ContentType.IMAGE, ContentType.VIDEO, ContentType.FILE, ContentType.AUDIO);
        } else if (allowPhoto || allowFile) {
            return Set.of(ContentType.TEXT, ContentType.IMAGE, ContentType.VIDEO, ContentType.FILE);
        } else {
            return Set.of(ContentType.TEXT);
        }
    }

    /** Belirtilen içerik tipine izin veriyor mu? */
    public boolean allowsContentType(ContentType type) {
        return getAllowedContentTypes().contains(type);
    }
}
