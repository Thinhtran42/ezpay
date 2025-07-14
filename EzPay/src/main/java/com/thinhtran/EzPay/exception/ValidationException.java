package com.thinhtran.EzPay.exception;

public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String message, Object data) {
        super("VALIDATION_ERROR", message, data);
    }
} 