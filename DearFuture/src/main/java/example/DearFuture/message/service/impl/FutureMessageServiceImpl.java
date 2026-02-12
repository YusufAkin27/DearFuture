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
import example.DearFuture.message.entity.ContentType;
import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.FutureMessageContent;
import example.DearFuture.message.entity.MessageStatus;
import example.DearFuture.message.mapper.FutureMessageMapper;
import example.DearFuture.message.repository.FutureMessageContentRepository;
import example.DearFuture.message.repository.FutureMessageRepository;
import example.DearFuture.message.service.FutureMessageService;
import example.DearFuture.payment.entity.SubscriptionPayment;
import example.DearFuture.payment.repository.SubscriptionPaymentRepository;
import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.SubscriptionPlanRepository;
import example.DearFuture.user.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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

    private final FutureMessageRepository futureMessageRepository;
    private final FutureMessageContentRepository futureMessageContentRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPaymentRepository paymentRepository;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public MessageResponse createMessage(CreateMessageRequest request) {
        User user = getCurrentUser();
        SubscriptionPlan plan = effectivePlan(user);
        validateMessageLimit(user, plan);
        FutureMessage message = new FutureMessage();
        message.setUser(user);
        message.setScheduledAt(request.getScheduledAt());
        message.setStatus(MessageStatus.SCHEDULED);
        message.setRecipientEmails(Collections.singletonList(user.getEmail()));
        message.setPublic(request.getIsPublic() != null && request.getIsPublic());
        
        // Create Content (Defaulting to TEXT)
        FutureMessageContent content = new FutureMessageContent();
        content.setType(ContentType.TEXT);
        content.setTextContent(request.getContent());
        content.setFutureMessage(message);
        
        message.setContents(Collections.singletonList(content));
        
        FutureMessage savedMessage = futureMessageRepository.save(message);
        
        return MessageResponse.fromEntity(savedMessage);
    }

    public MessageResponse getMessage(Long id) {
        User user = getCurrentUser();
        FutureMessage message = futureMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not authorized to view this message");
        }
        return MessageResponse.fromEntity(message);
    }

    @Transactional
    public MessageResponse updateMessage(Long id, example.DearFuture.message.dto.request.UpdateMessageRequest request) {
        User user = getCurrentUser();
        FutureMessage message = futureMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not authorized to edit this message");
        }

        if (message.getStatus() != MessageStatus.SCHEDULED) {
            throw new IllegalArgumentException("Cannot edit a message that is not in SCHEDULED status");
        }

        if (effectivePlan(user) != null && effectivePlan(user).isFree()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Ücretsiz hesaplar bekleyen mesajlarını düzenleyemez.");
        }

        if (request.getContent() != null) {
            // Update first content text for now
            if (!message.getContents().isEmpty()) {
                message.getContents().get(0).setTextContent(request.getContent());
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
        FutureMessage message = futureMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not authorized to delete this message");
        }

        if (message.getStatus() == MessageStatus.SCHEDULED && effectivePlan(user) != null && effectivePlan(user).isFree()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Ücretsiz hesaplar bekleyen mesajlarını silemez.");
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
        User user = getCurrentUser();
        SubscriptionPlan plan = effectivePlan(user);
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

        futureMessageRepository.save(futureMessage);
        return ResponseEntity.ok("Message scheduled");
    }

    private SubscriptionPlan effectivePlan(User user) {
        if (user.getSubscriptionEndsAt() != null && java.time.LocalDateTime.now().isAfter(user.getSubscriptionEndsAt())) {
            return getFreePlan();
        }
        return user.getSubscriptionPlan() != null ? user.getSubscriptionPlan() : getFreePlan();
    }

    private SubscriptionPlan getFreePlan() {
        return planRepository.findByCode("FREE").orElse(null);
    }

    private void validateMessageLimit(User user, SubscriptionPlan plan) {
        if (plan != null && plan.isFree()) {
            long totalCount = futureMessageRepository.countByUserAndStatusIn(user,
                    Set.of(MessageStatus.SCHEDULED, MessageStatus.QUEUED, MessageStatus.SENT));
            if (totalCount >= plan.getMaxMessages()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_MESSAGE_LIMIT_EXCEEDED,
                        "Ücretsiz hesapta bekleyen ve iletilen mesajların toplamı en fazla " + plan.getMaxMessages() + " olabilir. Yeni mesaj kaydedemezsiniz.");
            }
        } else {
            var period = getCurrentPeriodForUser(user);
            long usedInPeriod = futureMessageRepository.countByUserIdAndScheduledAtBetween(user.getId(), period.start(), period.end());
            if (usedInPeriod >= plan.getMaxMessages()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_MESSAGE_LIMIT_EXCEEDED,
                        "Bu dönemde " + plan.getMaxMessages() + " mesaj hakkınızı doldurdunuz. Sonraki dönemde yenilenir.");
            }
        }
    }

    /** Ücretli plan kullanıcısı için mevcut dönem başlangıç ve bitiş (Instant). */
    private record Period(Instant start, Instant end) {}

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

    private void validateAttachmentLimits(SubscriptionPlan plan, List<MessageContentRequest> contents) {
        long imageCount = contents.stream().filter(c -> c.getType() == ContentType.IMAGE).count();
        long fileCount = contents.stream().filter(c -> c.getType() == ContentType.FILE).count();
        if (plan.getMaxPhotosPerMessage() > 0 && imageCount > plan.getMaxPhotosPerMessage()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Planınızda mesaj başına en fazla " + plan.getMaxPhotosPerMessage() + " fotoğraf ekleyebilirsiniz.");
        }
        if (plan.getMaxFilesPerMessage() > 0 && fileCount > plan.getMaxFilesPerMessage()) {
            throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                    "Planınızda mesaj başına en fazla " + plan.getMaxFilesPerMessage() + " dosya ekleyebilirsiniz.");
        }
        for (MessageContentRequest c : contents) {
            if (c.getType() == ContentType.IMAGE && c.getFileSize() != null && plan.getMaxPhotoSizeBytes() > 0
                    && c.getFileSize() > plan.getMaxPhotoSizeBytes()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Fotoğraf boyutu " + (plan.getMaxPhotoSizeBytes() / (1024 * 1024)) + " MB'dan küçük olmalıdır.");
            }
            if (c.getType() == ContentType.FILE && c.getFileSize() != null && plan.getMaxFileSizeBytes() > 0
                    && c.getFileSize() > plan.getMaxFileSizeBytes()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Dosya boyutu " + (plan.getMaxFileSizeBytes() / (1024 * 1024)) + " MB'dan küçük olmalıdır.");
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
        SubscriptionPlan plan = effectivePlan(user);
        String typeUpper = type != null ? type.toUpperCase() : "";
        if ("IMAGE".equals(typeUpper)) {
            if (!plan.isAllowPhoto()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Fotoğraf ekleme planınızda yok. Plus veya Premium'a yükseltin.");
            }
            if (file.getSize() > plan.getMaxPhotoSizeBytes()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Fotoğraf boyutu en fazla " + (plan.getMaxPhotoSizeBytes() / (1024 * 1024)) + " MB olabilir.");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Sadece görsel dosyaları yükleyebilirsiniz.");
            }
        } else if ("FILE".equals(typeUpper)) {
            if (!plan.isAllowFile()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Dosya ekleme planınızda yok. Plus veya Premium'a yükseltin.");
            }
            if (file.getSize() > plan.getMaxFileSizeBytes()) {
                throw new PlanLimitExceededException(ErrorCode.PLAN_FEATURE_NOT_AVAILABLE,
                        "Dosya boyutu en fazla " + (plan.getMaxFileSizeBytes() / (1024 * 1024)) + " MB olabilir.");
            }
        } else {
            throw new IllegalArgumentException("type IMAGE veya FILE olmalıdır.");
        }
        try {
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String publicId = "msg-" + user.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
            @SuppressWarnings("unchecked")
            Map<String, Object> opts = ObjectUtils.asMap(
                    "folder", CLOUDINARY_MESSAGES_FOLDER,
                    "public_id", publicId
            );
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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal(); 
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
