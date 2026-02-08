package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class InvalidTokenException extends BaseException {

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(ErrorCode.INVALID_TOKEN, message, cause);
    }
}
