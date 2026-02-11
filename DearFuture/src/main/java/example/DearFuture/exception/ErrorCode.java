package example.DearFuture.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_TOKEN("AUTH_001", "Invalid token"),
    TOKEN_EXPIRED("AUTH_002", "Token expired"),
    TOKEN_REVOKED("AUTH_003", "Token revoked"),
    TOKEN_BLACKLISTED("AUTH_004", "Token blacklisted"),
    INVALID_CREDENTIALS("AUTH_005", "Invalid credentials"),
    USER_NOT_FOUND("AUTH_006", "User not found"),
    USERNAME_ALREADY_EXISTS("AUTH_007", "Username already exists"),
    REFRESH_TOKEN_NOT_FOUND("AUTH_008", "Refresh token not found"),
    RATE_LIMIT_EXCEEDED("AUTH_009", "Rate limit exceeded"),
    ACCOUNT_LOCKED("AUTH_010", "Account locked"),
    DEVICE_MISMATCH("AUTH_011", "Device mismatch detected"),
    IP_MISMATCH("AUTH_012", "IP address mismatch detected"),
    TOKEN_REUSE_DETECTED("AUTH_013", "Token reuse detected"),
    VERIFICATION_REQUIRED("AUTH_014", "Verification required"),
    CODE_EXPIRED("AUTH_015", "Verification code expired"),
    CODE_ALREADY_USED("AUTH_016", "Verification code already used"),
    INVALID_CODE("AUTH_017", "Invalid verification code"),

    VALIDATION_ERROR("VALID_001", "Validation error"),

    /**
     * Plan mesaj limiti aşıldı
     */
    PLAN_MESSAGE_LIMIT_EXCEEDED("PLAN_001", "Message limit exceeded for your plan"),
    /**
     * Bu içerik tipi (fotoğraf/dosya/ses) planınızda yok
     */
    PLAN_FEATURE_NOT_AVAILABLE("PLAN_002", "This feature is not available in your plan"),
    /**
     * Alıcı sayısı plan limitini aşıyor
     */
    PLAN_RECIPIENT_LIMIT_EXCEEDED("PLAN_003", "Recipient limit exceeded for your plan"),

    INTERNAL_ERROR("SYS_001", "Internal server error"),
    RESOURCENOTFOUND("CONTRACT_001", "Resource not found"),
    ;

    private final String code;
    private final String message;
}
