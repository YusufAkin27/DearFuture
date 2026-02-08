package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class TokenRevokedException extends BaseException {

    public TokenRevokedException(String message) {
        super(ErrorCode.TOKEN_REVOKED, message);
    }

    public TokenRevokedException(String message, Throwable cause) {
        super(ErrorCode.TOKEN_REVOKED, message, cause);
    }
}
