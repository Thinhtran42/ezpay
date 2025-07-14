package com.thinhtran.EzPay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OTPRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+84|84|0)(3|5|7|8|9)[0-9]{8}$", 
             message = "Invalid Vietnamese phone number format")
    private String phoneNumber;
} 