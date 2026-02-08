package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND, message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(ErrorCode.USER_NOT_FOUND, message, cause);
    }
}
