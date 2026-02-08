package example.DearFuture.exception.security;

import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class CodeAlreadyUsedException extends BaseException {
    public CodeAlreadyUsedException(String message) {
        super(ErrorCode.CODE_ALREADY_USED, message);
    }
}
