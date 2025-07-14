package com.thinhtran.EzPay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private Double totalTransferred;
    private Integer totalTransactions;
    private List<TopReceiverResponse> topReceivers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopReceiverResponse {
        private String username;
        private String fullName;
        private Double totalReceived;
        private Integer transactionCount;
    }
} 