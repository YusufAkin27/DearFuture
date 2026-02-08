package example.DearFuture.user.entity;

import example.DearFuture.message.entity.ContentType;

import java.util.Set;

/**
 * Kullanıcı abonelik planları.
 * FREE: Sadece metin, sınırlı mesaj.
 * PLUS: Fotoğraf + dosya, daha fazla mesaj ve alıcı.
 * PREMIUM: Ses kaydı dahil, en yüksek limitler.
 */
public enum SubscriptionPlan {

    FREE(3, 1, false, false, false, 0, 0L, 0, 0L),
    PLUS(20, 5, true, true, false, 2, 5L * 1024 * 1024, 2, 10L * 1024 * 1024),
    PREMIUM(100, 20, true, true, true, 5, 10L * 1024 * 1024, 5, 20L * 1024 * 1024);

    /** Plan başına maksimum aktif (zamanlanmış) mesaj sayısı */
    private final int maxMessages;
    /** Mesaj başına maksimum alıcı sayısı */
    private final int maxRecipientsPerMessage;
    /** Fotoğraf ekleme izni */
    private final boolean allowPhoto;
    /** Dosya ekleme izni */
    private final boolean allowFile;
    /** Ses kaydı ekleme izni */
    private final boolean allowVoice;
    /** Mesaj başına maksimum fotoğraf sayısı (PLUS/PREMIUM) */
    private final int maxPhotosPerMessage;
    /** Fotoğraf başına maksimum boyut (byte) */
    private final long maxPhotoSizeBytes;
    /** Mesaj başına maksimum dosya sayısı */
    private final int maxFilesPerMessage;
    /** Dosya başına maksimum boyut (byte) */
    private final long maxFileSizeBytes;

    SubscriptionPlan(int maxMessages, int maxRecipientsPerMessage,
                     boolean allowPhoto, boolean allowFile, boolean allowVoice,
                     int maxPhotosPerMessage, long maxPhotoSizeBytes, int maxFilesPerMessage, long maxFileSizeBytes) {
        this.maxMessages = maxMessages;
        this.maxRecipientsPerMessage = maxRecipientsPerMessage;
        this.allowPhoto = allowPhoto;
        this.allowFile = allowFile;
        this.allowVoice = allowVoice;
        this.maxPhotosPerMessage = maxPhotosPerMessage;
        this.maxPhotoSizeBytes = maxPhotoSizeBytes;
        this.maxFilesPerMessage = maxFilesPerMessage;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public int getMaxRecipientsPerMessage() {
        return maxRecipientsPerMessage;
    }

    public boolean isAllowPhoto() {
        return allowPhoto;
    }

    public boolean isAllowFile() {
        return allowFile;
    }

    public boolean isAllowVoice() {
        return allowVoice;
    }

    /**
     * Bu planın kullanabileceği içerik tiplerini döner.
     */
    public Set<ContentType> getAllowedContentTypes() {
        return switch (this) {
            case FREE -> Set.of(ContentType.TEXT);
            case PLUS -> Set.of(ContentType.TEXT, ContentType.IMAGE, ContentType.VIDEO, ContentType.FILE);
            case PREMIUM -> Set.of(ContentType.TEXT, ContentType.IMAGE, ContentType.VIDEO, ContentType.FILE, ContentType.AUDIO);
        };
    }

    public boolean allowsContentType(ContentType type) {
        return getAllowedContentTypes().contains(type);
    }

    public int getMaxPhotosPerMessage() {
        return maxPhotosPerMessage;
    }

    public long getMaxPhotoSizeBytes() {
        return maxPhotoSizeBytes;
    }

    public int getMaxFilesPerMessage() {
        return maxFilesPerMessage;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }
}
