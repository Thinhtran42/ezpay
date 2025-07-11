package com.thinhtran.EzPay.controller;

import com.thinhtran.EzPay.dto.request.TransferRequest;
import com.thinhtran.EzPay.dto.response.TransactionResponse;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> transfer(@AuthenticationPrincipal User user,
                                      @RequestBody TransferRequest request) {
        transactionService.transfer(user.getUserName(), request);
        return ResponseEntity.ok("Chuyển tiền thành công");
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> history(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.getHistory(user.getUserName()));
    }
}