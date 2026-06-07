package com.paymentgateway.fraud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargebackEvent {
    private String transactionId;
    private String merchantId;
    private String reasonCode; // e.g., "10.4" (Fraud)
    private Double amount;
    private String timestamp;
}
