package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class TokenExpiredException extends BaseException {

    public TokenExpiredException(String message) {
        super(ErrorCode.TOKEN_EXPIRED, message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(ErrorCode.TOKEN_EXPIRED, message, cause);
    }
}
