package example.DearFuture.user.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import example.DearFuture.exception.security.UserNotFoundException;
import example.DearFuture.message.repository.FutureMessageRepository;
import example.DearFuture.payment.repository.SubscriptionPaymentRepository;
import example.DearFuture.user.dto.request.UpdateProfileRequest;
import example.DearFuture.user.dto.request.UpdateSettingsRequest;
import example.DearFuture.user.dto.response.ProfileResponse;
import example.DearFuture.user.dto.response.UserResponse;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final FutureMessageRepository futureMessageRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;

    private static final String CLOUDINARY_PROFILE_FOLDER = "dearfuture/profiles";

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        return UserResponse.fromUser(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(Long request, @Valid UpdateProfileRequest updateProfileRequest) {
        Optional<User> optionalUser = userRepository.findById(request);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found: " + request);
        }
        User user = optionalUser.get();
        if (updateProfileRequest.getFirstName() != null) {
            user.setFirstName(updateProfileRequest.getFirstName());
        }
        if (updateProfileRequest.getLastName() != null) {
            user.setLastName(updateProfileRequest.getLastName());
        }
        return ProfileResponse.fromUser(user);
    }

    @Override
    @Transactional
    public UserResponse updateSettings(Long userId, @Valid UpdateSettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        if (request.getLocale() != null && !request.getLocale().isBlank()) {
            user.setLocale(request.getLocale());
        }
        if (request.getEmailNotifications() != null) {
            user.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getMarketingEmails() != null) {
            user.setMarketingEmails(request.getMarketingEmails());
        }
        userRepository.save(user);
        return UserResponse.fromUser(user);
    }

    @Override
    @Transactional
    public void uploadProfilePhoto(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Dosya seçiniz.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Sadece görsel dosyaları yükleyebilirsiniz.");
        }
        try {
            String shortId = "user-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8);
            @SuppressWarnings("unchecked")
            Map<String, Object> opts = ObjectUtils.asMap(
                    "folder", CLOUDINARY_PROFILE_FOLDER,
                    "public_id", shortId
            );
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), opts);
            String secureUrl = (String) result.get("secure_url");
            if (secureUrl == null) {
                throw new RuntimeException("Cloudinary secure_url alınamadı.");
            }
            user.setProfilePictureUrl(secureUrl);
            userRepository.save(user);
            log.info("Profile photo uploaded to Cloudinary for user {}", userId);
        } catch (IOException e) {
            log.error("Profile photo upload failed", e);
            throw new RuntimeException("Profil fotoğrafı yüklenemedi.");
        }
    }

    @Override
    @Transactional
    public void deleteProfilePhoto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        String url = user.getProfilePictureUrl();
        if (url != null && url.contains("res.cloudinary.com")) {
            try {
                String publicId = extractCloudinaryPublicId(url);
                if (publicId != null && !publicId.isEmpty()) {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    log.info("Profile photo deleted from Cloudinary for user {}", userId);
                }
            } catch (Exception e) {
                log.warn("Cloudinary delete failed for profile photo, clearing URL anyway", e);
            }
        }
        user.setProfilePictureUrl(null);
        userRepository.save(user);
    }

    /**
     * Cloudinary URL'den public_id çıkarır.
     * Örnek: .../upload/v123/dearfuture/profiles/user-1-abc.jpg -> dearfuture/profiles/user-1-abc
     */
    private String extractCloudinaryPublicId(String secureUrl) {
        if (secureUrl == null || !secureUrl.contains("/upload/")) return null;
        String afterUpload = secureUrl.substring(secureUrl.indexOf("/upload/") + 8);
        if (afterUpload.startsWith("v") && afterUpload.length() > 1) {
            int slash = afterUpload.indexOf('/');
            if (slash > 0) afterUpload = afterUpload.substring(slash + 1);
        }
        int lastDot = afterUpload.lastIndexOf('.');
        if (lastDot > 0) afterUpload = afterUpload.substring(0, lastDot);
        return afterUpload.isEmpty() ? null : afterUpload;
    }

    @Override
    @Transactional(readOnly = true)
    public String getProfilePhoto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        return user.getProfilePictureUrl() != null ? user.getProfilePictureUrl() : "";
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        futureMessageRepository.deleteAll(futureMessageRepository.findAllByUser(user));
        subscriptionPaymentRepository.deleteAll(subscriptionPaymentRepository.findByUser(user));
        userRepository.delete(user);
        log.info("Account deleted: userId={}", userId);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Account deactivated: userId={}", userId);
    }
}
