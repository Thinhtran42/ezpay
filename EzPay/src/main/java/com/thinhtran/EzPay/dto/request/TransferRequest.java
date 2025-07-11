package com.thinhtran.EzPay.dto.request;

import lombok.Data;

@Data
public class TransferRequest {
    private String receiverUsername;
    private Double amount;
    private String message;
}
