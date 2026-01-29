package com.paymentgateway.fraud.controller;

import com.paymentgateway.fraud.model.ModelDisagreement.DisagreementType;
import com.paymentgateway.fraud.repository.ModelDisagreementRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fraud/metrics")
@RequiredArgsConstructor
public class FraudMetricsController {

    private final ModelDisagreementRepository repository;

    @GetMapping("/disagreement-stats")
    public DisagreementStats getStats() {
        long total = repository.count();
        long disagreements = repository.countByType(DisagreementType.MISSED_FRAUD) +
                repository.countByType(DisagreementType.FALSE_POSITIVE);

        return DisagreementStats.builder()
                .bothFraud(repository.countByType(DisagreementType.BOTH_FRAUD))
                .bothLegit(repository.countByType(DisagreementType.BOTH_LEGIT))
                .missedFraud(repository.countByType(DisagreementType.MISSED_FRAUD))
                .falsePositive(repository.countByType(DisagreementType.FALSE_POSITIVE))
                .total(total)
                .disagreementRate(total > 0 ? (double) disagreements / total * 100.0 : 0.0)
                .build();
    }

    @Data
    @Builder
    public static class DisagreementStats {
        private long bothFraud;
        private long bothLegit;
        private long missedFraud;
        private long falsePositive;
        private long total;
        private double disagreementRate;
    }
}
