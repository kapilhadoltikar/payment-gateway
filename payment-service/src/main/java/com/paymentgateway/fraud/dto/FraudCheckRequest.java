package com.paymentgateway.fraud.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
