package example.DearFuture.exception.security;

import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class CodeExpiredException extends BaseException {
    public CodeExpiredException(String message) {
        super(ErrorCode.CODE_EXPIRED, message);
    }
}
