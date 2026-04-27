package com.paymentgateway.fraud.controller;

import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.dto.FraudResult;
import com.paymentgateway.fraud.service.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@WebFluxTest(controllers = FraudController.class)
@org.springframework.test.context.ActiveProfiles("test")
public class FraudControllerDocsTest {

        private WebTestClient webTestClient;

        @MockitoBean
        private FraudDetectionService fraudService;

        @BeforeEach
        public void setUp(ApplicationContext context,
                        RestDocumentationContextProvider restDocumentation) {
                this.webTestClient = WebTestClient.bindToApplicationContext(context)
                                .configureClient()
                                .filter(documentationConfiguration(restDocumentation))
                                .build();
        }

        @Test
        public void checkFraud() {
                FraudCheckRequest request = FraudCheckRequest.builder()
                                .transactionId("TXN-123")
                                .merchantId("MERCH-123")
                                .userId("USER-456")
                                .amount(300.0)
                                .currency("USD")
                                .email("fraud@test.com")
                                .build();

                FraudResult result = FraudResult.builder()
                                .transactionId("TXN-123")
                                .riskScore(0.9)
                                .decision(FraudResult.FraudDecision.BLOCK)
                                .build();

                when(fraudService.evaluateRisk(any())).thenReturn(Mono.just(result));

                this.webTestClient.post().uri("/api/v1/fraud/check")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .consumeWith(document("check-fraud",
                                                requestFields(
                                                                fieldWithPath("transactionId").description(
                                                                                "The ID of the transaction"),
                                                                fieldWithPath("merchantId")
                                                                                .description("The ID of the merchant"),
                                                                fieldWithPath("userId")
                                                                                .description("The ID of the user"),
                                                                fieldWithPath("amount")
                                                                                .description("The transaction amount"),
                                                                fieldWithPath("currency").description("The currency"),
                                                                fieldWithPath("email").description(
                                                                                "The email of the customer"),
                                                                fieldWithPath("ipAddress")
                                                                                .description("Optional IP address")
                                                                                .optional(),
                                                                fieldWithPath("deviceFingerprint").description(
                                                                                "Optional device fingerprint")
                                                                                .optional()),
                                                responseFields(
                                                                fieldWithPath("success").description(
                                                                                "Whether the operation was successful"),
                                                                fieldWithPath("message").description(
                                                                                "A message describing the result"),
                                                                fieldWithPath("data.transactionId").description(
                                                                                "The ID of the transaction"),
                                                                fieldWithPath("data.riskScore").description(
                                                                                "The fraud risk score (0.0 to 1.0)"),
                                                                fieldWithPath("data.decision")
                                                                                .description("The fraud decision (APPROVE, MANUAL_REVIEW, BLOCK)"),
                                                                fieldWithPath("data.riskFactors").description(
                                                                                "List of risk factors identified")
                                                                                .optional(),
                                                                fieldWithPath("errorCode")
                                                                                .description("The error code if failed")
                                                                                .optional(),
                                                                fieldWithPath("timestamp").description(
                                                                                "The response timestamp"))));
        }
}
