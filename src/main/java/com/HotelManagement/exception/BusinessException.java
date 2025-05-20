package com.HotelManagement.exception;

/**
 * Custom exception class that includes an error code
 */
public class BusinessException extends RuntimeException {
    private final String errorCode;
    
    public BusinessException(String errorCode) {
        super(ErrorCodes.getMessage(errorCode));
        this.errorCode = errorCode;
    }
    
    public BusinessException(String errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
} 