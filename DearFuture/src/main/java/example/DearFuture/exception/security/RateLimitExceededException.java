package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class RateLimitExceededException extends BaseException {
    public RateLimitExceededException(String message) {
        super(ErrorCode.RATE_LIMIT_EXCEEDED, message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(ErrorCode.RATE_LIMIT_EXCEEDED, message, cause);
    }
}
