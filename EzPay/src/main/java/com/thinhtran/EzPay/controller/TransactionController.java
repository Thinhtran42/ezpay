package com.thinhtran.EzPay.controller;

import com.thinhtran.EzPay.dto.request.TopUpRequest;
import com.thinhtran.EzPay.dto.request.TransferRequest;
import com.thinhtran.EzPay.dto.response.ApiResponse;
import com.thinhtran.EzPay.dto.response.StatisticsResponse;
import com.thinhtran.EzPay.dto.response.TransactionResponse;
import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.exception.AccessDeniedException;
import com.thinhtran.EzPay.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> transfer(@AuthenticationPrincipal User user,
                                                     @Valid @RequestBody TransferRequest request) {
        transactionService.transfer(user.getUserName(), request);
        return ResponseEntity.ok(ApiResponse.success("Chuyển tiền thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> history(@AuthenticationPrincipal User user) {
        List<TransactionResponse> history = transactionService.getHistory(user.getUserName());
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử giao dịch thành công", history));
    }

    @PostMapping("/top-up")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> topUp(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody TopUpRequest request) {
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied. Admin role required.");
        }
        transactionService.topUp(request);
        return ResponseEntity.ok(ApiResponse.success("Nạp tiền thành công"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics(@AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied. Admin role required.");
        }
        StatisticsResponse statistics = transactionService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê thành công", statistics));
    }
}