package com.contractreview.reviewengine.domain.exception;

import com.contractreview.exception.enums.ErrorCode;

/**
 * 业务异常
 *
 * @author SaltyFish
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}