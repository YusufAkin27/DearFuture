package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class TokenReuseException extends BaseException {
    public TokenReuseException(String message) {
        super(ErrorCode.TOKEN_REUSE_DETECTED, message);
    }

    public TokenReuseException(String message, Throwable cause) {
        super(ErrorCode.TOKEN_REUSE_DETECTED, message, cause);
    }
}
