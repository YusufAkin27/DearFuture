package example.DearFuture.exception.security;


import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class DeviceMismatchException extends BaseException {
    public DeviceMismatchException(String message) {
        super(ErrorCode.DEVICE_MISMATCH, message);
    }

    public DeviceMismatchException(String message, Throwable cause) {
        super(ErrorCode.DEVICE_MISMATCH, message, cause);
    }
}
