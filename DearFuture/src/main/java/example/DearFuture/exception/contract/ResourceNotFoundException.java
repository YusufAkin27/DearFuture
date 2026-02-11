package example.DearFuture.exception.contract;

import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCENOTFOUND, message);
    }
}
