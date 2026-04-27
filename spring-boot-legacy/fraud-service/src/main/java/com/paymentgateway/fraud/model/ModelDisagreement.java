package com.paymentgateway.fraud.model;

import com.paymentgateway.fraud.dto.FraudResult.FraudDecision;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "model_disagreements", indexes = {
        @Index(name = "idx_disagreement_type", columnList = "type"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelDisagreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    private Double championScore;
    private Double challengerScore;

    @Enumerated(EnumType.STRING)
    private FraudDecision championDecision;

    @Enumerated(EnumType.STRING)
    private FraudDecision challengerDecision;

    @Enumerated(EnumType.STRING)
    private DisagreementType type;

    private Long championInferenceTimeMs;
    private Long challengerInferenceTimeMs;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DisagreementType {
        BOTH_FRAUD, // Both said BLOCK
        BOTH_LEGIT, // Both said APPROVE
        MISSED_FRAUD, // Champion: APPROVE, Challenger: BLOCK
        FALSE_POSITIVE, // Champion: BLOCK, Challenger: APPROVE
        AGREEMENT // Generic agreement
    }
}
