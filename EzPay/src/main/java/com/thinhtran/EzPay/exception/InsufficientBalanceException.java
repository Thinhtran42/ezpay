package com.thinhtran.EzPay.exception;

public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException(Double currentBalance, Double requiredAmount) {
        super("INSUFFICIENT_BALANCE", 
              String.format("Số dư không đủ. Số dư hiện tại: %.2f, Số tiền cần: %.2f", 
                          currentBalance, requiredAmount));
    }

    public InsufficientBalanceException(String message) {
        super("INSUFFICIENT_BALANCE", message);
    }
} 