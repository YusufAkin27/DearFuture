package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(ErrorCode.INVALID_CREDENTIALS, message, cause);
    }
}
