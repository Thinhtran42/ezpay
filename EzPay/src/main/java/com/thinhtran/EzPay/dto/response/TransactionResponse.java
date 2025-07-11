package com.thinhtran.EzPay.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private String senderUsername;
    private String receiverUsername;
    private Double amount;
    private String message;
    private LocalDateTime createdAt;
}
