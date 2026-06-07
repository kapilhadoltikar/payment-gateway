package com.paymentgateway.fraud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudResult {
    private String transactionId;
    private double riskScore;
    private FraudDecision decision;
    private List<String> riskFactors;

    public enum FraudDecision {
        APPROVE, // Green Path
        MANUAL_REVIEW, // Gray Path
        BLOCK // Red Path
    }
}
