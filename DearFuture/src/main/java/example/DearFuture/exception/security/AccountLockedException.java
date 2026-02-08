package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class AccountLockedException extends BaseException {
    public AccountLockedException(String message) {
        super(ErrorCode.ACCOUNT_LOCKED, message);
    }

    public AccountLockedException(String message, Throwable cause) {
        super(ErrorCode.ACCOUNT_LOCKED, message, cause);
    }
}
