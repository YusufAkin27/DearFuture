package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class RefreshTokenNotFoundException extends BaseException {

    public RefreshTokenNotFoundException(String message) {
        super(ErrorCode.REFRESH_TOKEN_NOT_FOUND, message);
    }

    public RefreshTokenNotFoundException(String message, Throwable cause) {
        super(ErrorCode.REFRESH_TOKEN_NOT_FOUND, message, cause);
    }
}
