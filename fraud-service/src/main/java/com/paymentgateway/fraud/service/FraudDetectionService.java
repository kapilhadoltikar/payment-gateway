package com.paymentgateway.fraud.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.dto.FraudResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.FloatBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;

@Service
@Slf4j
public class FraudDetectionService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final OrtEnvironment env;
    private final OrtSession session;

    public FraudDetectionService(ReactiveStringRedisTemplate redisTemplate, OrtEnvironment env,
            @Autowired(required = false) OrtSession session) {
        this.redisTemplate = redisTemplate;
        this.env = env;
        this.session = session;
    }

    public Mono<FraudResult> evaluateRisk(FraudCheckRequest request) {
        log.info("Evaluating risk for request: {}", request);
        // 1. Cold Start / Rule-Based Filter
        if (isColdStart(request)) {
            return Mono.just(applyColdStartRules(request));
        }

        // 2. Feature Enrichment (Concurrent)
        return fetchFeatures(request)
                .flatMap(features -> {
                    // 3. AI Inference
                    try (var scope = StructuredTaskScope.open()) {
                        var inferenceTask = scope.fork(() -> runInference(features));

                        scope.join();

                        double score = inferenceTask.get();
                        return Mono.just(buildDecision(request, score));
                    } catch (Exception e) {
                        log.error("AI Inference failed, falling back to rule engine", e);
                        return Mono.just(fallbackDecision(request));
                    }
                });
    }

    private Mono<float[]> fetchFeatures(FraudCheckRequest request) {
        String velocityKey = "velocity:" + request.getUserId();

        // Simulate fetching User Profile for history (e.g. avg amounts)
        // In reality, this would be a separate Redis GET or a "mget"
        return redisTemplate.opsForValue().increment(velocityKey)
                .doOnNext(count -> redisTemplate.expire(velocityKey, Duration.ofHours(1)).subscribe())
                .map(count -> {
                    float[] features = new float[11];

                    // 1. Transaction Amount
                    features[0] = request.getAmount().floatValue();

                    // 2. Velocity (1h count)
                    features[1] = count.floatValue();

                    // 3. Time of Day (Is Night? > 10PM or < 6AM)
                    java.time.LocalTime now = java.time.LocalTime.now();
                    boolean isNight = now.getHour() >= 22 || now.getHour() < 6;
                    features[2] = isNight ? 1.0f : 0.0f;

                    // 4. Amount Delta (Deviation from Mock Avg $100)
                    float avgTicket = 100.0f;
                    features[3] = (float) Math.abs(request.getAmount() - avgTicket);

                    // 5. New Device Check (Mocked from request)
                    features[4] = (request.getDeviceFingerprint() == null) ? 1.0f : 0.0f;

                    // Fill rest with 0.0
                    for (int i = 5; i < 11; i++)
                        features[i] = 0.0f;

                    return features;
                });
    }

    private double runInference(float[] features) throws OrtException {
        if (session == null)
            return 0.05; // Default low risk if no model

        long[] shape = new long[] { 1, features.length };
        try (OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(features), shape)) {
            try (OrtSession.Result result = session.run(Collections.singletonMap("input", tensor))) {
                float[][] output = (float[][]) result.get(0).getValue();
                return (double) output[0][0]; // Propability of Fraud
            }
        }
    }

    private FraudResult buildDecision(FraudCheckRequest request, double score) {
        FraudResult.FraudDecision decision;
        List<String> factors = new ArrayList<>();

        if (score > 0.8) {
            decision = FraudResult.FraudDecision.BLOCK;
            factors.add("High AI Risk Score");
        } else if (score > 0.3) {
            decision = FraudResult.FraudDecision.MANUAL_REVIEW;
            factors.add("Medium AI Risk Score");
        } else {
            decision = FraudResult.FraudDecision.APPROVE;
        }

        return FraudResult.builder()
                .transactionId(request.getTransactionId())
                .riskScore(score)
                .decision(decision)
                .riskFactors(factors)
                .build();
    }

    // --- Cold Start Logic ---

    private boolean isColdStart(FraudCheckRequest request) {
        // Simplified: if no userId or new user (mocked)
        return request.getUserId() == null || request.getUserId().startsWith("new_");
    }

    private FraudResult applyColdStartRules(FraudCheckRequest request) {
        log.info("Applying Cold Start rules for user {}", request.getUserId());
        log.info("Amount for Cold Start Check: {}", request.getAmount());

        if (request.getAmount() > 200.0) {
            log.info("Decision: BLOCK (> 200.0)");
            return FraudResult.builder()
                    .transactionId(request.getTransactionId())
                    .riskScore(0.9)
                    .decision(FraudResult.FraudDecision.BLOCK)
                    .riskFactors(List.of("Cold Start Limit Exceeded (> $200)"))
                    .build();
        }
        return FraudResult.builder()
                .transactionId(request.getTransactionId())
                .riskScore(0.1)
                .decision(FraudResult.FraudDecision.APPROVE)
                .riskFactors(List.of("Cold Start - Safe Amount"))
                .build();
    }

    private FraudResult fallbackDecision(FraudCheckRequest request) {
        return FraudResult.builder()
                .transactionId(request.getTransactionId())
                .riskScore(0.5)
                .decision(FraudResult.FraudDecision.MANUAL_REVIEW)
                .riskFactors(List.of("System Fallback"))
                .build();
    }
}
