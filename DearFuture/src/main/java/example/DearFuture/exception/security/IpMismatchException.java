package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class IpMismatchException extends BaseException {
    public IpMismatchException(String message) {
        super(ErrorCode.IP_MISMATCH, message);
    }

    public IpMismatchException(String message, Throwable cause) {
        super(ErrorCode.IP_MISMATCH, message, cause);
    }
}
