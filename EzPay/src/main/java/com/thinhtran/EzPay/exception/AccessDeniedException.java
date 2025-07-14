package com.thinhtran.EzPay.exception;

public class AccessDeniedException extends BusinessException {
    public AccessDeniedException(String message) {
        super("ACCESS_DENIED", message);
    }

    public AccessDeniedException() {
        super("ACCESS_DENIED", "Access denied. You don't have permission to perform this action.");
    }
} 