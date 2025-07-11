package com.thinhtran.EzPay.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private String userName;
    private String fullName;
    private String email;
    private Double balance;
}