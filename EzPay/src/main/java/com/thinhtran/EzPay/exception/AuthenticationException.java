package com.thinhtran.EzPay.exception;

public class AuthenticationException extends BusinessException {
    public AuthenticationException(String message) {
        super("AUTHENTICATION_ERROR", message);
    }

    public AuthenticationException() {
        super("AUTHENTICATION_ERROR", "Invalid username or password");
    }
} 