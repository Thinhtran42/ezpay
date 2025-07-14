package com.thinhtran.EzPay.exception;

public class DuplicateDataException extends BusinessException {
    public DuplicateDataException(String field, String value) {
        super("DUPLICATE_DATA", String.format("%s '%s' already exists", field, value));
    }

    public DuplicateDataException(String message) {
        super("DUPLICATE_DATA", message);
    }
} 