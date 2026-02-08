package example.DearFuture.exception.subscription;

import example.DearFuture.exception.BaseException;
import example.DearFuture.exception.ErrorCode;

public class PlanLimitExceededException extends BaseException {

    public PlanLimitExceededException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public PlanLimitExceededException(ErrorCode errorCode, String message, java.util.Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
