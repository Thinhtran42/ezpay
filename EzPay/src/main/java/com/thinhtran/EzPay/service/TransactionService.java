package com.thinhtran.EzPay.service;

import com.thinhtran.EzPay.dto.request.TransferRequest;
import com.thinhtran.EzPay.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {
    void transfer(String senderUsername, TransferRequest request);

    List<TransactionResponse> getHistory(String username);
}
