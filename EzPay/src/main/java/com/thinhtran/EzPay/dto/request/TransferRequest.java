package com.thinhtran.EzPay.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TransferRequest {
    @NotBlank(message = "Receiver username is required")
    @Size(min = 3, max = 50, message = "Receiver username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Receiver username can only contain letters, numbers, underscore and dash")
    private String receiverUsername;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @DecimalMax(value = "999999999.99", message = "Amount cannot exceed 999,999,999.99")
    @Digits(integer = 9, fraction = 2, message = "Amount must have at most 9 integer digits and 2 decimal places")
    private Double amount;

    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;
}
