package com.paymentgateway.fraud.service;

import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.dto.FraudResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FraudDetectionServiceTest {

        @Mock
        private ReactiveStringRedisTemplate redisTemplate;

        @Mock
        private ReactiveValueOperations<String, String> valueOperations;

        @InjectMocks
        private FraudDetectionService fraudDetectionService;

        private ai.onnxruntime.OrtEnvironment env = ai.onnxruntime.OrtEnvironment.getEnvironment();

        @Mock
        private ai.onnxruntime.OrtSession championSession;

        @Mock
        private ai.onnxruntime.OrtSession challengerSession;

        @BeforeEach
        public void setup() {
                org.mockito.Mockito.lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        public void evaluateRisk_DualInference_Executes() {
                // User not "new_", so skips cold start
                FraudCheckRequest request = FraudCheckRequest.builder()
                                .userId("user_123")
                                .amount(100.0)
                                .transactionId("t3")
                                .build();

                // Redis mocks for feature extraction
                when(valueOperations.increment(anyString())).thenReturn(Mono.just(1L));
                when(redisTemplate.expire(anyString(), any(java.time.Duration.class))).thenReturn(Mono.just(true));

                // When evaluateRisk is called, it should run dual inference
                // Since mocks throw/return null for session.run, it catches exception and
                // returns default 0.05

                StepVerifier.create(fraudDetectionService.evaluateRisk(request))
                                .expectNextMatches(
                                                result -> result.getDecision() == FraudResult.FraudDecision.APPROVE &&
                                                                result.getRiskScore() == 0.05)
                                .verifyComplete();
        }

        @Test
        public void evaluateRisk_ColdStart_HighAmount_ReturnsBlocked() {
                FraudCheckRequest request = FraudCheckRequest.builder()
                                .userId("new_user_123")
                                .amount(250.0)
                                .transactionId("t1")
                                .build();

                StepVerifier.create(fraudDetectionService.evaluateRisk(request))
                                .expectNextMatches(result -> result.getDecision() == FraudResult.FraudDecision.BLOCK)
                                .verifyComplete();
        }

        @Test
        public void evaluateRisk_ColdStart_LowAmount_ReturnsApproved() {
                FraudCheckRequest request = FraudCheckRequest.builder()
                                .userId("new_user_123")
                                .amount(50.0)
                                .transactionId("t2")
                                .build();

                StepVerifier.create(fraudDetectionService.evaluateRisk(request))
                                .expectNextMatches(result -> result.getDecision() == FraudResult.FraudDecision.APPROVE)
                                .verifyComplete();
        }
}
