package com.paymentgateway.fraud;

import com.paymentgateway.fraud.FraudServiceApplication;
import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.common.dto.ApiResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FraudServiceApplication.class)
@ActiveProfiles("test")
public class FraudDetectionTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @org.springframework.test.context.bean.override.mockito.MockitoBean
        private org.springframework.data.redis.core.ReactiveStringRedisTemplate redisTemplate;

        @org.springframework.test.context.bean.override.mockito.MockitoBean
        private org.springframework.data.redis.core.ReactiveValueOperations<String, String> valueOperations;

        @org.junit.jupiter.api.BeforeEach
        void setup() {
                org.mockito.Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
                org.mockito.Mockito.when(valueOperations.increment((String) org.mockito.ArgumentMatchers.anyString()))
                                .thenReturn(reactor.core.publisher.Mono.just(1L));
                org.mockito.Mockito
                                .when(redisTemplate.expire((String) org.mockito.ArgumentMatchers.anyString(),
                                                (java.time.Duration) org.mockito.ArgumentMatchers
                                                                .any(java.time.Duration.class)))
                                .thenReturn(reactor.core.publisher.Mono.just(true));
        }

        @Test
        void shouldAssessFraudRisk() {
                // Given
                FraudCheckRequest request = new FraudCheckRequest();
                request.setTransactionId(UUID.randomUUID().toString());
                request.setAmount(500.00); // Double as per DTO
                request.setMerchantId("MERCHANT-XYZ");
                request.setIpAddress("192.168.1.1");
                request.setUserId("USER-123");
                // request.setCardHash("some-card-hash"); // Field not in DTO

                // When
                ResponseEntity<ApiResponse<com.paymentgateway.fraud.dto.FraudResult>> response = restTemplate.exchange(
                                "/api/v1/fraud/check",
                                org.springframework.http.HttpMethod.POST,
                                new org.springframework.http.HttpEntity<>(request),
                                new org.springframework.core.ParameterizedTypeReference<ApiResponse<com.paymentgateway.fraud.dto.FraudResult>>() {
                                });

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().isSuccess()).isTrue();

                // Extract data from ApiResponse
                com.paymentgateway.fraud.dto.FraudResult data = response.getBody().getData();
                double riskScore = data.getRiskScore();
                assertThat(riskScore).isGreaterThanOrEqualTo(0);
                assertThat(riskScore).isLessThanOrEqualTo(100);
        }
}
