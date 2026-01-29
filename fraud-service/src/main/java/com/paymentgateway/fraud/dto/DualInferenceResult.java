package com.paymentgateway.fraud.dto;

import com.paymentgateway.fraud.dto.FraudResult.FraudDecision;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DualInferenceResult {
    private double championScore;
    private double challengerScore;

    private FraudDecision championDecision;
    private FraudDecision challengerDecision;

    private long championInferenceTimeMs;
    private long challengerInferenceTimeMs;
}
