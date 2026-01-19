package com.paymentgateway.fraud.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckRequest {
    private String transactionId;
    private String merchantId;
    private String userId;
    private Double amount;
    private String currency;
    private String ipAddress;
    private String deviceFingerprint;
    private String email;
}
