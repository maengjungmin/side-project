package org.side.mjm.common.exception.custom;

import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.code.CustomErrorCode;
import org.side.mjm.common.exception.code.ErrorCode;

import java.io.Serial;

public class ServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    public final ErrorCode errorCode;

    public ServiceException(CommonErrorCode errorCode, Throwable cause) {
        super(cause.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ServiceException(CommonErrorCode errorCode, String message) {
        super(message, null);
        this.errorCode = errorCode;
    }

    public ServiceException(CommonErrorCode errorCode) {
        super(errorCode.getResultMsg(), null);
        this.errorCode = errorCode;
    }

    public ServiceException(CustomErrorCode errorCode) {
        super(errorCode.getResultMsg(), null);
        this.errorCode = errorCode;
    }
}
