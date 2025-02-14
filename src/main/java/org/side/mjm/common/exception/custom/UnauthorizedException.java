package org.side.mjm.common.exception.custom;

import org.side.mjm.common.exception.code.CommonErrorCode;

import java.io.Serializable;

public class UnauthorizedException extends RuntimeException implements Serializable {

    private final CommonErrorCode commonErrorCode;
    private final Throwable cause;

    public UnauthorizedException(CommonErrorCode commonErrorCode, Throwable cause) {
        super(commonErrorCode.getResultMsg());
        this.commonErrorCode = commonErrorCode;
        this.cause = cause;
    }

    public CommonErrorCode code() {
        return this.commonErrorCode;
    }

    public Throwable cause() {
        return this.cause;
    }
}
