package example.DearFuture.message.service.impl;

import example.DearFuture.exception.contract.ResourceNotFoundException;
import example.DearFuture.exception.security.UserNotFoundException;
import example.DearFuture.message.dto.response.PublicMessageItemResponse;
import example.DearFuture.message.entity.ContentType;
import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.MessageStatus;
import example.DearFuture.message.entity.StarredPublicMessage;
import example.DearFuture.message.repository.FutureMessageRepository;
import example.DearFuture.message.repository.StarredPublicMessageRepository;
import example.DearFuture.message.service.PublicMessageService;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicMessageServiceImpl implements PublicMessageService {

    private static final int TEXT_PREVIEW_MAX_LENGTH = 200;

    private final FutureMessageRepository futureMessageRepository;
    private final StarredPublicMessageRepository starredRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PublicMessageItemResponse> getPublicMessages(Long currentUserId) {
        Instant now = Instant.now();
        List<FutureMessage> messages = futureMessageRepository.findPublicMessagesUnlocked(now);
        Set<Long> starredIds = currentUserId != null
                ? starredRepository.findFutureMessageIdsByUserId(currentUserId).stream()
                .collect(Collectors.toSet())
                : Set.of();

        return messages.stream()
                .map(m -> toPublicItem(m, starredIds.contains(m.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicMessageItemResponse> getPublicMessages(Long currentUserId, Pageable pageable) {
        Instant now = Instant.now();
        Page<FutureMessage> page = futureMessageRepository.findPublicMessagesUnlocked(now, pageable);
        Set<Long> starredIds = currentUserId != null
                ? starredRepository.findFutureMessageIdsByUserId(currentUserId).stream()
                .collect(Collectors.toSet())
                : Set.of();
        List<PublicMessageItemResponse> content = page.getContent().stream()
                .map(m -> toPublicItem(m, starredIds.contains(m.getId())))
                .collect(Collectors.toList());
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicMessageItemResponse> getMyStarredMessages() {
        User user = getCurrentUser();
        List<StarredPublicMessage> starred = starredRepository.findByUserIdWithFutureMessageOrderByCreatedAtDesc(user.getId());
        return starred.stream()
                .map(StarredPublicMessage::getFutureMessage)
                .map(m -> toPublicItem(m, true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void starMessage(Long messageId) {
        User user = getCurrentUser();
        FutureMessage message = futureMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Mesaj bulunamadı."));
        if (!message.isPublic()
                || (message.getStatus() != MessageStatus.SENT && message.getStatus() != MessageStatus.QUEUED)) {
            throw new IllegalArgumentException("Bu mesaj yıldızlanamaz.");
        }
        if (message.getScheduledAt().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Bu mesaj henüz açılmamış.");
        }
        if (starredRepository.existsByUserIdAndFutureMessageId(user.getId(), messageId)) {
            return; // zaten yıldızlı
        }
        StarredPublicMessage star = StarredPublicMessage.builder()
                .user(user)
                .futureMessage(message)
                .createdAt(Instant.now())
                .build();
        starredRepository.save(star);
    }

    @Override
    @Transactional
    public void unstarMessage(Long messageId) {
        User user = getCurrentUser();
        starredRepository.deleteByUserIdAndFutureMessageId(user.getId(), messageId);
    }

    private PublicMessageItemResponse toPublicItem(FutureMessage m, boolean starredByMe) {
        String senderName = null;
        if (m.getUser() != null) {
            String first = m.getUser().getFirstName();
            String last = m.getUser().getLastName();
            if (first != null && !first.isBlank()) {
                senderName = first.trim();
                if (last != null && !last.isBlank()) {
                    senderName += " " + last.trim();
                }
            }
        }
        String textPreview = null;
        if (m.getContents() != null) {
            textPreview = m.getContents().stream()
                    .filter(c -> c.getType() == ContentType.TEXT && c.getTextContent() != null)
                    .map(c -> c.getTextContent().length() > TEXT_PREVIEW_MAX_LENGTH
                            ? c.getTextContent().substring(0, TEXT_PREVIEW_MAX_LENGTH) + "..."
                            : c.getTextContent())
                    .findFirst()
                    .orElse(null);
        }
        return PublicMessageItemResponse.builder()
                .id(m.getId())
                .viewToken(m.getViewToken())
                .scheduledAt(m.getScheduledAt())
                .sentAt(m.getSentAt())
                .senderName(senderName)
                .textPreview(textPreview)
                .starredByMe(starredByMe)
                .build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new UserNotFoundException("Giriş yapmanız gerekiyor.");
        }
        Long userId = (Long) auth.getPrincipal();
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı."));
    }
}
