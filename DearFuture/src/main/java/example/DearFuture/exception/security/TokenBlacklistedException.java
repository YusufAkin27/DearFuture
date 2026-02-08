package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class TokenBlacklistedException extends BaseException {

    public TokenBlacklistedException(String message) {
        super(ErrorCode.TOKEN_BLACKLISTED, message);
    }

    public TokenBlacklistedException(String message, Throwable cause) {
        super(ErrorCode.TOKEN_BLACKLISTED, message, cause);
    }
}
