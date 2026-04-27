package com.paymentgateway.common.exception;

import lombok.Getter;

/**
 * Standardized business exception for domain-specific errors
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public BusinessException(String message) {
        this(message, "BUSINESS_ERROR", 400);
    }

    public BusinessException(String message, String errorCode) {
        this(message, errorCode, 400);
    }

    public BusinessException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String message, Throwable cause, String errorCode, int httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
