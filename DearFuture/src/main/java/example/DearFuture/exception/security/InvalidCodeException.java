package example.DearFuture.exception.security;

import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidCodeException extends BaseException {
    public InvalidCodeException(String message) {
        super(ErrorCode.INVALID_CODE, message);
    }
}
