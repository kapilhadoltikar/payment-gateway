package com.paymentgateway.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckRequest {
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private String currency;
    private String ipAddress;
    private String deviceFingerprint;
    private String email;
}
