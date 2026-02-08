package example.DearFuture.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ===================== BASE EXCEPTION ===================== */

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = determineHttpStatus(ex);

        log.warn(
                "Hata: {} - {} ({})",
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                request.getRequestURI()
        );

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errorCode(ex.getErrorCode().getCode())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    /* ===================== AUTHENTICATION ===================== */

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication hatası: {}", ex.getMessage());

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("Authentication failed")
                .path(request.getRequestURI())
                .errorCode(ErrorCode.INVALID_CREDENTIALS.getCode())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    /* ===================== ACCESS DENIED ===================== */

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("You don't have permission to access this resource")
                .path(request.getRequestURI())
                .errorCode(ErrorCode.INVALID_CREDENTIALS.getCode())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    /* ===================== VALIDATION ===================== */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, Object> details = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            details.put(field, error.getDefaultMessage());
        });

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("Validation failed")
                .path(request.getRequestURI())
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .details(details)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    /* ===================== GENERIC ===================== */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Beklenmeyen hata", ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .errorCode(ErrorCode.INTERNAL_ERROR.getCode())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    /* ===================== STATUS MAPPING ===================== */

    private HttpStatus determineHttpStatus(BaseException ex) {
        return switch (ex.getErrorCode()) {
            case INVALID_TOKEN, TOKEN_EXPIRED, TOKEN_REVOKED, TOKEN_BLACKLISTED,
                 INVALID_CREDENTIALS, USER_NOT_FOUND, REFRESH_TOKEN_NOT_FOUND,
                 DEVICE_MISMATCH, IP_MISMATCH, TOKEN_REUSE_DETECTED,
                 VERIFICATION_REQUIRED, INVALID_CODE -> HttpStatus.UNAUTHORIZED;

            case USERNAME_ALREADY_EXISTS, VALIDATION_ERROR,
                 CODE_EXPIRED, CODE_ALREADY_USED -> HttpStatus.BAD_REQUEST;

            case PLAN_MESSAGE_LIMIT_EXCEEDED, PLAN_FEATURE_NOT_AVAILABLE, PLAN_RECIPIENT_LIMIT_EXCEEDED -> HttpStatus.FORBIDDEN;

            case RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case ACCOUNT_LOCKED -> HttpStatus.LOCKED;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
