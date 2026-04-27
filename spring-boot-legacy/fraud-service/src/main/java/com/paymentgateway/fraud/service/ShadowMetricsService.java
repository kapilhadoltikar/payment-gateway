package com.paymentgateway.fraud.service;

import com.paymentgateway.fraud.dto.DualInferenceResult;
import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.model.ModelDisagreement;
import com.paymentgateway.fraud.model.ModelDisagreement.DisagreementType;
import com.paymentgateway.fraud.repository.ModelDisagreementRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShadowMetricsService {

        private final ModelDisagreementRepository repository;
        private final MeterRegistry meterRegistry;

        @Async
        public void logDisagreement(FraudCheckRequest request, DualInferenceResult result) {
                try {
                        DisagreementType type = classifyDisagreement(result);

                        // Only log true disagreements or a 1% sample of agreements for baseline
                        boolean isDisagreement = (type == DisagreementType.MISSED_FRAUD
                                        || type == DisagreementType.FALSE_POSITIVE);
                        boolean isSample = Math.random() < 0.01;

                        if (!isDisagreement && !isSample) {
                                return;
                        }

                        ModelDisagreement disagreement = ModelDisagreement.builder()
                                        .transactionId(request.getTransactionId())
                                        .championScore(result.getChampionScore())
                                        .challengerScore(result.getChallengerScore())
                                        .championDecision(result.getChampionDecision())
                                        .challengerDecision(result.getChallengerDecision())
                                        .championInferenceTimeMs(result.getChampionInferenceTimeMs())
                                        .challengerInferenceTimeMs(result.getChallengerInferenceTimeMs())
                                        .type(type)
                                        .build();

                        repository.save(Objects.requireNonNull(disagreement));

                        meterRegistry.counter("fraud.model.disagreement", "type", type.name()).increment();

                        log.debug("Logged model disagreement: {} (Champion: {}, Challenger: {})",
                                        type, result.getChampionDecision(), result.getChallengerDecision());

                } catch (Exception e) {
                        log.warn("Failed to log shadow metrics", e);
                }
        }

        private DisagreementType classifyDisagreement(DualInferenceResult result) {
                var champ = result.getChampionDecision();
                var chall = result.getChallengerDecision();

                boolean champBlock = isBlock(champ);
                boolean challBlock = isBlock(chall);

                if (champBlock && challBlock)
                        return DisagreementType.BOTH_FRAUD;
                if (!champBlock && !challBlock)
                        return DisagreementType.BOTH_LEGIT;
                if (!champBlock && challBlock)
                        return DisagreementType.MISSED_FRAUD; // Champion missed it
                if (champBlock && !challBlock)
                        return DisagreementType.FALSE_POSITIVE; // Champion was too aggressive

                return DisagreementType.AGREEMENT;
        }

        private boolean isBlock(com.paymentgateway.fraud.dto.FraudResult.FraudDecision d) {
                return d == com.paymentgateway.fraud.dto.FraudResult.FraudDecision.BLOCK ||
                                d == com.paymentgateway.fraud.dto.FraudResult.FraudDecision.MANUAL_REVIEW;
        }
}
