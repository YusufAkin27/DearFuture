package example.DearFuture.user.service;

import example.DearFuture.user.dto.request.UpdateProfileRequest;
import example.DearFuture.user.dto.request.UpdateSettingsRequest;
import example.DearFuture.user.dto.response.ProfileResponse;
import example.DearFuture.user.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponse getProfile(Long userId);

    ProfileResponse updateProfile(Long userId, @Valid UpdateProfileRequest request);

    UserResponse updateSettings(Long userId, @Valid UpdateSettingsRequest request);

        void uploadProfilePhoto(Long userId, MultipartFile file);

        void deleteProfilePhoto(Long userId);

        String getProfilePhoto(Long userId);

        void deleteAccount(Long userId);

        void deactivateAccount(Long userId);
    }
