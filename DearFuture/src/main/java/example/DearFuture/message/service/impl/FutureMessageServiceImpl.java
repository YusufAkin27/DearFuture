package example.DearFuture.message.service.impl;

import example.DearFuture.exception.contract.ResourceNotFoundException;
import example.DearFuture.exception.security.UserNotFoundException;
import example.DearFuture.exception.subscription.PlanLimitExceededException;
import example.DearFuture.exception.ErrorCode;
import example.DearFuture.message.dto.request.CreateFutureMessageRequest;
import example.DearFuture.message.dto.request.CreateMessageRequest;
import example.DearFuture.message.dto.request.MessageContentRequest;
import example.DearFuture.message.dto.response.MessageResponse;
import example.DearFuture.message.dto.response.MessageUploadResponse;
import example.DearFuture.user.dto.response.MessageQuotaResponse;
import example.DearFuture.message.encryption.MessageEncryptionService;
import example.DearFuture.message.entity.ContentType;
import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.FutureMessageContent;
import example.DearFuture.message.entity.MessageStatus;
import example.DearFuture.message.mapper.FutureMessageMapper;
import example.DearFuture.message.repository.FutureMessageRepository;
import example.DearFuture.message.service.FutureMessageService;
import example.DearFuture.payment.entity.SubscriptionPayment;
import example.DearFuture.payment.repository.SubscriptionPaymentRepository;
import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.SubscriptionPlanRepository;
import example.DearFuture.user.repository.UserRepository;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FutureMessageServiceImpl implements FutureMessageService {

    private static final String CLOUDINARY_MESSAGES_FOLDER = "dearfuture/messages";

    /**
     * İzin verilen ses formatları (AUDIO yüklemeleri).
     */
    private static final Set<String> ALLOWED_AUDIO_EXTENSIONS = Set.of("mp3", "wav", "ogg", "m4a", "aac", "webm", "opus");
    private static final Set<String> ALLOWED_AUDIO_CONTENT_TYPE_PREFIXES = Set.of("audio/mpeg", "audio/mp3", "audio/wav", "audio/x-wav", "audio/ogg", "audio/webm", "audio/mp4", "audio/aac", "audio/x-m4a", "audio/opus");

    /** İzin verilen video uzantıları (VIDEO yüklemeleri). */
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of("mp4", "webm", "mov", "avi", "mkv", "m4v");

    /**
     * İzin verilen dosya uzantıları (FILE yüklemeleri): belge, arşiv, veri vb.
     */
    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "docm", "xls", "xlsx", "xlsm", "ppt", "pptx", "pptm",
            "odt", "ods", "odp", "odg", "odf", "odm", "odb",
            "txt", "rtf", "csv", "md", "tex",
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz",
            "json", "xml", "yaml", "yml",
            "epub", "ics"
    );

    private final FutureMessageRepository futureMessageRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPaymentRepository paymentRepository;
    private final Cloudinary cloudinary;
    private final MessageEncryptionService messageEncryptionService;

    @Override
    @Transactional
    public MessageResponse createMessage(CreateMessageRequest request) {
        User user = getCurrentUser();
        SubscriptionPlan plan = ensurePlan(effectivePlan(user));
        if (plan == null) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE, "Abonelik planı yüklenemedi.");
        }
        validateMessageLimit(user, plan);
        FutureMessage message = new FutureMessage();
        message.setUser(user);
        message.setScheduledAt(request.getScheduledAt());
        message.setStatus(MessageStatus.SCHEDULED);
        message.setRecipientEmails(new java.util.ArrayList<>(Collections.singletonList(user.getEmail())));
        message.setPublic(request.getIsPublic() != null && request.getIsPublic());

        FutureMessageContent content = new FutureMessageContent();
        content.setType(ContentType.TEXT);
        String rawText = request.getContent();
        content.setTextContent(messageEncryptionService.isEnabled() && rawText != null && !rawText.isBlank()
                ? messageEncryptionService.encrypt(rawText)
                : rawText);
        content.setFutureMessage(message);

        message.setContents(new java.util.ArrayList<>(Collections.singletonList(content)));

        FutureMessage savedMessage = futureMessageRepository.save(message);

        return MessageResponse.fromEntity(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getMessage(Long id) {
        User user = getCurrentUser();
        FutureMessage message = futureMessageRepository.findByIdWithContents(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not authorized to view this message");
        }
        return MessageResponse.fromEntity(message);
    }

    @Transactional
    public MessageResponse updateMessage(Long id, example.DearFuture.message.dto.request.UpdateMessageRequest request) {
        User user = getCurrentUser();
        FutureMessage message = futureMessageRepository.findByIdWithContents(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not authorized to edit this message");
        }

        if (message.getStatus() != MessageStatus.SCHEDULED) {
            throw new IllegalArgumentException("Cannot edit a message that is not in SCHEDULED status");
        }

        SubscriptionPlan plan = ensurePlan(effectivePlan(user));
        if (plan != null && plan.isFree()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Ücretsiz hesaplar bekleyen mesajlarını düzenleyemez.");
        }

        if (request.getContent() != null) {
            if (!message.getContents().isEmpty()) {
                String raw = request.getContent();
                FutureMessageContent first = message.getContents().get(0);
                first.setTextContent(messageEncryptionService.isEnabled() && raw != null && !raw.isBlank()
                        ? messageEncryptionService.encrypt(raw)
                        : raw);
            }
        }
        if (request.getScheduledAt() != null) {
            message.setScheduledAt(request.getScheduledAt());
        }
        if (request.getIsPublic() != null) {
            message.setPublic(request.getIsPublic());
        }

        return MessageResponse.fromEntity(futureMessageRepository.save(message));
    }

    @Transactional
    public String deleteMessage(Long id) {
        User user = getCurrentUser();
        FutureMessage message = futureMessageRepository.findByIdWithContents(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not authorized to delete this message");
        }

        SubscriptionPlan plan = ensurePlan(effectivePlan(user));
        if (plan != null && plan.isFree()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Ücretsiz hesaplar mevcut mesajları (bekleyen veya iletilen) silemez.");
        }

        futureMessageRepository.delete(message);
        return "Message deleted";
    }


    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getPendingMessages() {
        User user = getCurrentUser();
        return futureMessageRepository.findAllByUserAndScheduledAtAfterOrderByScheduledAtAscWithContents(user, Instant.now())
                .stream()
                .map(MessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getDeliveredMessages() {
        User user = getCurrentUser();
        return futureMessageRepository.findAllByUserAndScheduledAtBeforeOrderByScheduledAtDescWithContents(user, Instant.now())
                .stream()
                .map(MessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResponseEntity<String> scheduleMessage(CreateFutureMessageRequest request) {
        if (request.getContents() == null || request.getContents().isEmpty()) {
            throw new IllegalArgumentException("Mesaj içeriği boş olamaz.");
        }
        User user = getCurrentUser();
        SubscriptionPlan plan = ensurePlan(effectivePlan(user));
        if (plan == null) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE, "Abonelik planı yüklenemedi.");
        }
        validateMessageLimit(user, plan);
        if (request.getRecipientEmails() != null && request.getRecipientEmails().size() > plan.getMaxRecipientsPerMessage()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_RECIPIENT_LIMIT_EXCEEDED,
                    "Maximum " + plan.getMaxRecipientsPerMessage() + " recipients per message for your plan.");
        }
        for (MessageContentRequest contentReq : request.getContents()) {
            if (!plan.allowsContentType(contentReq.getType())) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Content type " + contentReq.getType() + " is not available in your plan. Upgrade to add photos, files or voice.");
            }
        }
        validateAttachmentLimits(plan, request.getContents());
        FutureMessage futureMessage = FutureMessageMapper.toEntity(request);
        futureMessage.setUser(user);
        for (FutureMessageContent c : futureMessage.getContents()) {
            if (c.getType() == ContentType.TEXT && c.getTextContent() != null && !c.getTextContent().isBlank()
                    && messageEncryptionService.isEnabled()) {
                c.setTextContent(messageEncryptionService.encrypt(c.getTextContent()));
            }
            // Fotoğraf, video, ses ve dosya URL'lerini şifrele (Cloudinary linki DB'de düz metin kalmasın)
            if (c.getFileUrl() != null && !c.getFileUrl().isBlank() && messageEncryptionService.isEnabled()
                    && !messageEncryptionService.isEncrypted(c.getFileUrl())) {
                c.setFileUrl(messageEncryptionService.encrypt(c.getFileUrl()));
            }
        }

        futureMessageRepository.save(futureMessage);
        return ResponseEntity.ok("Message scheduled");
    }

    private SubscriptionPlan effectivePlan(User user) {
        if (user.getSubscriptionEndsAt() != null && LocalDateTime.now().isAfter(user.getSubscriptionEndsAt())) {
            return getFreePlan();
        }
        return user.getSubscriptionPlan() != null ? user.getSubscriptionPlan() : getFreePlan();
    }

    private SubscriptionPlan getFreePlan() {
        return planRepository.findByCode("FREE").orElse(null);
    }

    /**
     * Abonelik planı null ise FREE planı döndürür (dinamik kontrol için).
     */
    private SubscriptionPlan ensurePlan(SubscriptionPlan plan) {
        return plan != null ? plan : getFreePlan();
    }

    /**
     * Kullanıcının planına göre kalan mesaj hakkını döndürür (long).
     * FREE: toplam (SCHEDULED+QUEUED+SENT) sayıya göre limit - used.
     * PLUS/PREMIUM: dönem içi scheduledAt sayımına göre limit - usedInPeriod.
     */
    private long getRemainingMessageCount(User user, SubscriptionPlan plan) {
        if (plan == null) return 0L;
        int limit = plan.getMaxMessages();
        long used;
        if (plan.isFree()) {
            used = futureMessageRepository.countByUserAndStatusIn(user,
                    Set.of(MessageStatus.SCHEDULED, MessageStatus.QUEUED, MessageStatus.SENT));
        } else {
            var period = getCurrentPeriodForUser(user);
            used = futureMessageRepository.countByUserIdAndScheduledAtBetween(user.getId(), period.start(), period.end());
        }
        return Math.max(0L, limit - used);
    }

    /**
     * Mesaj kaydetme hakkı kontrolü. Kalan hak 0 veya negatifse exception fırlatır.
     * @return Kalan mesaj hakkı (long)
     */
    private long validateMessageLimit(User user, SubscriptionPlan plan) {
        long remaining = getRemainingMessageCount(user, plan);
        if (remaining <= 0) {
            if (plan != null && plan.isFree()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_MESSAGE_LIMIT_EXCEEDED,
                        "Ücretsiz hesapta bekleyen ve iletilen mesajların toplamı en fazla " + plan.getMaxMessages() + " olabilir. Yeni mesaj kaydedemezsiniz.");
            } else {
                throw new PlanLimitExceededException(ErrorCode.PLAN_MESSAGE_LIMIT_EXCEEDED,
                        "Bu dönemde " + (plan != null ? plan.getMaxMessages() : 0) + " mesaj hakkınızı doldurdunuz. Sonraki dönemde yenilenir.");
            }
        }
        return remaining;
    }

    /**
     * Ücretli plan kullanıcısı için mevcut dönem başlangıç ve bitiş (Instant).
     */
    private record Period(Instant start, Instant end) {
    }

    private Period getCurrentPeriodForUser(User user) {
        var lastPayment = paymentRepository.findTopByUserIdAndStatusOrderByPaidAtDesc(user.getId(), SubscriptionPayment.PaymentStatus.SUCCESS);
        if (lastPayment.isPresent() && lastPayment.get().getPeriodEnd() != null && lastPayment.get().getPeriodStart() != null
                && LocalDateTime.now().isBefore(lastPayment.get().getPeriodEnd())) {
            return new Period(
                    lastPayment.get().getPeriodStart().atZone(ZoneId.systemDefault()).toInstant(),
                    lastPayment.get().getPeriodEnd().atZone(ZoneId.systemDefault()).toInstant()
            );
        }
        LocalDateTime endLdt = user.getSubscriptionEndsAt() != null ? user.getSubscriptionEndsAt() : LocalDateTime.now().plusMonths(1);
        LocalDateTime startLdt = endLdt.minusMonths(1);
        return new Period(startLdt.atZone(ZoneId.systemDefault()).toInstant(), endLdt.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Mesaj eklerini plan verisine göre dinamik kontrol eder.
     * Fotoğraf + video aynı kotada (maxPhotosPerMessage, maxPhotoSizeBytes). Dosya ve ses ayrı plan alanlarından.
     */
    private void validateAttachmentLimits(SubscriptionPlan plan, List<MessageContentRequest> contents) {
        if (plan == null) return;
        long imageCount = contents.stream().filter(c -> c.getType() == ContentType.IMAGE).count();
        long videoCount = contents.stream().filter(c -> c.getType() == ContentType.VIDEO).count();
        long fileCount = contents.stream().filter(c -> c.getType() == ContentType.FILE).count();
        long audioCount = contents.stream().filter(c -> c.getType() == ContentType.AUDIO).count();
        long visualCount = imageCount + videoCount;

        if (visualCount > 0 && !plan.isAllowPhoto()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Planınızda fotoğraf/video ekleme yok. Plus veya Premium'a yükseltin.");
        }
        if (fileCount > 0 && !plan.isAllowFile()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Planınızda dosya ekleme yok. Plus veya Premium'a yükseltin.");
        }
        if (audioCount > 0 && !plan.isAllowVoice()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Planınızda ses kaydı ekleme yok. Premium'a yükseltin.");
        }

        if (plan.getMaxPhotosPerMessage() > 0 && visualCount > plan.getMaxPhotosPerMessage()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Planınızda mesaj başına en fazla " + plan.getMaxPhotosPerMessage() + " fotoğraf/video ekleyebilirsiniz.");
        }
        if (plan.getMaxFilesPerMessage() > 0 && fileCount > plan.getMaxFilesPerMessage()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Planınızda mesaj başına en fazla " + plan.getMaxFilesPerMessage() + " dosya ekleyebilirsiniz.");
        }
        if (plan.getMaxAudioPerMessage() > 0 && audioCount > plan.getMaxAudioPerMessage()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Planınızda mesaj başına en fazla " + plan.getMaxAudioPerMessage() + " ses kaydı ekleyebilirsiniz.");
        }

        long maxAudioSizeBytes = plan.getMaxAudioSizeBytes() > 0 ? plan.getMaxAudioSizeBytes() : plan.getMaxFileSizeBytes();
        for (MessageContentRequest c : contents) {
            if ((c.getType() == ContentType.IMAGE || c.getType() == ContentType.VIDEO) && c.getFileSize() != null
                    && plan.getMaxPhotoSizeBytes() > 0 && c.getFileSize() > plan.getMaxPhotoSizeBytes()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Fotoğraf/video boyutu en fazla " + (plan.getMaxPhotoSizeBytes() / (1024 * 1024)) + " MB olabilir.");
            }
            if (c.getType() == ContentType.FILE && c.getFileSize() != null && plan.getMaxFileSizeBytes() > 0
                    && c.getFileSize() > plan.getMaxFileSizeBytes()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Dosya boyutu en fazla " + (plan.getMaxFileSizeBytes() / (1024 * 1024)) + " MB olabilir.");
            }
            if (c.getType() == ContentType.AUDIO && c.getFileSize() != null && maxAudioSizeBytes > 0
                    && c.getFileSize() > maxAudioSizeBytes) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Ses kaydı boyutu en fazla " + (maxAudioSizeBytes / (1024 * 1024)) + " MB olabilir.");
            }
        }
    }

    @Override
    @Transactional
    public MessageUploadResponse uploadAttachment(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Dosya seçiniz.");
        }
        User user = getCurrentUser();
        SubscriptionPlan plan = ensurePlan(effectivePlan(user));
        if (plan == null) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE, "Abonelik planı yüklenemedi.");
        }
        String typeUpper = type != null ? type.toUpperCase() : "";
        if ("IMAGE".equals(typeUpper)) {
            if (!plan.isAllowPhoto()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Fotoğraf/video ekleme planınızda yok. Plus veya Premium'a yükseltin.");
            }
            if (plan.getMaxPhotoSizeBytes() > 0 && file.getSize() > plan.getMaxPhotoSizeBytes()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Fotoğraf boyutu en fazla " + (plan.getMaxPhotoSizeBytes() / (1024 * 1024)) + " MB olabilir.");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Sadece görsel dosyaları (resim) yükleyebilirsiniz.");
            }
        } else if ("VIDEO".equals(typeUpper)) {
            if (!plan.isAllowPhoto()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Fotoğraf/video ekleme planınızda yok. Plus veya Premium'a yükseltin.");
            }
            if (plan.getMaxPhotoSizeBytes() > 0 && file.getSize() > plan.getMaxPhotoSizeBytes()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Video boyutu en fazla " + (plan.getMaxPhotoSizeBytes() / (1024 * 1024)) + " MB olabilir.");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                throw new IllegalArgumentException("Sadece video dosyaları yükleyebilirsiniz (MP4, WebM, MOV vb.).");
            }
            String videoExt = getFileExtension(file.getOriginalFilename());
            if (videoExt == null || !ALLOWED_VIDEO_EXTENSIONS.contains(videoExt.toLowerCase())) {
                throw new IllegalArgumentException(
                        "Desteklenmeyen video formatı. İzin verilenler: MP4, WebM, MOV, AVI, MKV, M4V.");
            }
        } else if ("FILE".equals(typeUpper)) {
            if (!plan.isAllowFile()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Dosya ekleme planınızda yok. Plus veya Premium'a yükseltin.");
            }
            if (plan.getMaxFileSizeBytes() > 0 && file.getSize() > plan.getMaxFileSizeBytes()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Dosya boyutu en fazla " + (plan.getMaxFileSizeBytes() / (1024 * 1024)) + " MB olabilir.");
            }
            String ext = getFileExtension(file.getOriginalFilename());
            if (ext == null || !ALLOWED_FILE_EXTENSIONS.contains(ext.toLowerCase())) {
                throw new IllegalArgumentException(
                        "Desteklenmeyen dosya türü. İzin verilenler: PDF, Word, Excel, PowerPoint, " +
                                "OpenDocument, TXT, RTF, CSV, MD, ZIP, RAR, 7Z, TAR, GZ, JSON, XML, EPUB vb.");
            }
        } else if ("AUDIO".equals(typeUpper)) {
            if (!plan.isAllowVoice()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Ses kaydı ekleme planınızda yok. Premium'a yükseltin.");
            }
            long maxAudioSize = plan.getMaxAudioSizeBytes() > 0 ? plan.getMaxAudioSizeBytes() : plan.getMaxFileSizeBytes();
            if (maxAudioSize > 0 && file.getSize() > maxAudioSize) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Ses kaydı boyutu en fazla " + (maxAudioSize / (1024 * 1024)) + " MB olabilir.");
            }
            String contentType = file.getContentType();
            if (contentType == null || !isAllowedAudioContentType(contentType)) {
                throw new IllegalArgumentException(
                        "Sadece ses dosyaları yükleyebilirsiniz (MP3, WAV, OGG, M4A, AAC, WebM, Opus).");
            }
            String audioExt = getFileExtension(file.getOriginalFilename());
            if (audioExt == null || !ALLOWED_AUDIO_EXTENSIONS.contains(audioExt.toLowerCase())) {
                throw new IllegalArgumentException(
                        "Desteklenmeyen ses formatı. İzin verilenler: MP3, WAV, OGG, M4A, AAC, WebM, Opus.");
            }
        } else {
            throw new IllegalArgumentException("type IMAGE, VIDEO, FILE veya AUDIO olmalıdır.");
        }
        try {
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String publicId = "msg-" + user.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
            Map<String, Object> opts = new java.util.HashMap<>();
            opts.put("folder", CLOUDINARY_MESSAGES_FOLDER);
            opts.put("public_id", publicId);
            if ("VIDEO".equals(typeUpper)) {
                opts.put("resource_type", "video");
            } else if ("FILE".equals(typeUpper) || "AUDIO".equals(typeUpper)) {
                opts.put("resource_type", "raw");
            }
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), opts);
            String secureUrl = (String) result.get("secure_url");
            if (secureUrl == null) {
                throw new IllegalArgumentException("Dosya yüklenemedi.");
            }
            return MessageUploadResponse.builder()
                    .url(secureUrl)
                    .fileName(originalFilename)
                    .fileSize(file.getSize())
                    .build();
        } catch (IOException e) {
            throw new IllegalArgumentException("Dosya yüklenemedi: " + e.getMessage());
        }
    }

    private static boolean isAllowedAudioContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) return false;
        String ct = contentType.toLowerCase().split(";")[0].trim();
        return ALLOWED_AUDIO_CONTENT_TYPE_PREFIXES.contains(ct) || ct.startsWith("audio/");
    }

    private static String getFileExtension(String filename) {
        if (filename == null || filename.isBlank()) return null;
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) return null;
        return filename.substring(lastDot + 1).trim();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageQuotaResponse getMessageQuota(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));
        SubscriptionPlan plan = ensurePlan(effectivePlan(user));
        int limit = plan != null ? plan.getMaxMessages() : 0;
        long remaining = getRemainingMessageCount(user, plan);
        long used = Math.max(0L, limit - remaining);
        return MessageQuotaResponse.builder()
                .limit(limit)
                .used(used)
                .remaining(remaining)
                .planCode(plan != null ? plan.getCode() : "FREE")
                .planName(plan != null ? plan.getName() : "Ücretsiz")
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
