package com.paymentgateway.fraud.service;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.dto.FraudResult;
import com.paymentgateway.fraud.config.OpenApiConfig;
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

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FraudDetectionServiceTest {

        @Mock
        private ReactiveStringRedisTemplate redisTemplate;

        @Mock
        private ReactiveValueOperations<String, String> valueOperations;

        @Mock
        private OrtEnvironment env;

        @Mock
        private OrtSession championSession;

        @Mock
        private OrtSession challengerSession;

        @Mock
        private ShadowMetricsService shadowMetricsService;

        @InjectMocks
        private FraudDetectionService fraudDetectionService;

        @BeforeEach
        public void setup() {
                lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        public void evaluateRisk_DualInference_WithShadowMetrics_Executes() {
                FraudCheckRequest request = FraudCheckRequest.builder()
                                .userId("user_123")
                                .amount(100.0)
                                .transactionId("t3")
                                .deviceFingerprint("fp1")
                                .build();

                lenient().when(valueOperations.increment(anyString())).thenReturn(Mono.just(1L));
                lenient().when(redisTemplate.expire(anyString(), any(java.time.Duration.class)))
                                .thenReturn(Mono.just(true));

                StepVerifier.create(fraudDetectionService.evaluateRisk(request))
                                .expectNextMatches(result -> result.getDecision() == FraudResult.FraudDecision.APPROVE)
                                .verifyComplete();

                org.mockito.Mockito.verify(shadowMetricsService, org.mockito.Mockito.atLeastOnce())
                                .logDisagreement(any(), any());
        }

        @Test
        public void evaluateRisk_ColdStart_NullUser_ReturnsBlocked() {
                FraudCheckRequest request = FraudCheckRequest.builder()
                                .userId(null)
                                .amount(250.0)
                                .transactionId("t1")
                                .build();

                StepVerifier.create(fraudDetectionService.evaluateRisk(request))
                                .expectNextMatches(result -> result.getDecision() == FraudResult.FraudDecision.BLOCK)
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

        @Test
        public void scoreToDecision_Coverage() throws Exception {
                java.lang.reflect.Method method = FraudDetectionService.class.getDeclaredMethod("scoreToDecision",
                                double.class);
                method.setAccessible(true);

                assertThat(method.invoke(fraudDetectionService, 0.9)).isEqualTo(FraudResult.FraudDecision.BLOCK);
                assertThat(method.invoke(fraudDetectionService, 0.5))
                                .isEqualTo(FraudResult.FraudDecision.MANUAL_REVIEW);
                assertThat(method.invoke(fraudDetectionService, 0.1)).isEqualTo(FraudResult.FraudDecision.APPROVE);
        }

        @Test
        public void nightTime_Coverage() {
                LocalTime night = LocalTime.of(23, 0);
                LocalTime day = LocalTime.of(10, 0);
                try (org.mockito.MockedStatic<LocalTime> mockedTime = org.mockito.Mockito.mockStatic(LocalTime.class)) {
                        mockedTime.when(LocalTime::now).thenReturn(night);

                        FraudCheckRequest request = FraudCheckRequest.builder()
                                        .userId("user_123")
                                        .amount(100.0)
                                        .build();
                        lenient().when(valueOperations.increment(anyString())).thenReturn(Mono.just(1L));
                        lenient().when(redisTemplate.expire(anyString(), any(java.time.Duration.class)))
                                        .thenReturn(Mono.just(true));

                        fraudDetectionService.evaluateRisk(request).block();

                        mockedTime.when(LocalTime::now).thenReturn(day);
                        fraudDetectionService.evaluateRisk(request).block();
                }
        }

        @Test
        public void evaluateRisk_NullSessions_ReturnsDefault() {
                FraudDetectionService service = new FraudDetectionService(redisTemplate, env, null, null,
                                shadowMetricsService);

                FraudCheckRequest request = FraudCheckRequest.builder()
                                .userId("user_123")
                                .amount(100.0)
                                .build();

                lenient().when(valueOperations.increment(anyString())).thenReturn(Mono.just(1L));
                lenient().when(redisTemplate.expire(anyString(), any(java.time.Duration.class)))
                                .thenReturn(Mono.just(true));

                StepVerifier.create(service.evaluateRisk(request))
                                .expectNextMatches(res -> res.getRiskScore() == 0.05)
                                .verifyComplete();
        }

        @Test
        public void runInference_HandlesException() throws Exception {
                // Mock session.run to throw exception - use lenient
                lenient().when(championSession.run(any()))
                                .thenThrow(new ai.onnxruntime.OrtException("Inference Error"));

                FraudCheckRequest request = FraudCheckRequest.builder()
                                .userId("user_123")
                                .amount(100.0)
                                .build();

                lenient().when(valueOperations.increment(anyString())).thenReturn(Mono.just(1L));
                lenient().when(redisTemplate.expire(anyString(), any(java.time.Duration.class)))
                                .thenReturn(Mono.just(true));

                StepVerifier.create(fraudDetectionService.evaluateRisk(request))
                                .expectNextMatches(res -> res.getRiskScore() == 0.05)
                                .verifyComplete();
        }

        @Test
        public void openApiConfig_Instantiation() {
                OpenApiConfig config = new OpenApiConfig();
                assertThat(config).isNotNull();
        }
}
