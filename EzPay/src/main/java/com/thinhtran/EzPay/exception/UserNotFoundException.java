package com.thinhtran.EzPay.exception;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(String username) {
        super("USER_NOT_FOUND", "User not found: " + username);
    }

    public UserNotFoundException(String message, Object data) {
        super("USER_NOT_FOUND", message, data);
    }
} 