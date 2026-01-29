package com.paymentgateway.fraud.integration;

import com.paymentgateway.fraud.FraudServiceApplication;
import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.dto.FraudResult;
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
public class FraudDetectionIT {

    @Autowired
    private TestRestTemplate restTemplate;

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
        ResponseEntity<FraudResult> response = restTemplate.postForEntity("/api/v1/fraud/check", request,
                FraudResult.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // FraudResult likely has riskScore or similar
        assertThat(response.getBody().getRiskScore()).isGreaterThanOrEqualTo(0);
        assertThat(response.getBody().getRiskScore()).isLessThanOrEqualTo(100);
    }
}
