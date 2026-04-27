package com.paymentgateway.fraud.controller;

import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.dto.FraudResult;
import com.paymentgateway.fraud.service.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
public class FraudControllerTest {

        private WebTestClient webTestClient;

        @Autowired
        private FraudController fraudController;

        @MockitoBean
        private FraudDetectionService fraudDetectionService;

        @BeforeEach
        void setUp() {
                webTestClient = WebTestClient.bindToController(fraudController).build();
        }

        @Test
        public void checkFraud_ReturnsResult() throws Exception {
                FraudCheckRequest request = FraudCheckRequest.builder()
                                .transactionId("t1")
                                .merchantId("m1")
                                .userId("u1")
                                .amount(100.0)
                                .email("user@example.com")
                                .build();

                FraudResult response = FraudResult.builder()
                                .transactionId("t1")
                                .decision(FraudResult.FraudDecision.APPROVE)
                                .build();

                when(fraudDetectionService.evaluateRisk(any(FraudCheckRequest.class))).thenReturn(Mono.just(response));

                webTestClient.post().uri("/api/v1/fraud/check")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.success").isEqualTo(true)
                                .jsonPath("$.data.decision").isEqualTo("APPROVE");
        }
}
