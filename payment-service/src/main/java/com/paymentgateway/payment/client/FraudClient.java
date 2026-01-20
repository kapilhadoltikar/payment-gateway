package com.paymentgateway.payment.client;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.dto.FraudResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudClient {

    private final RestTemplate restTemplate;

    @Value("${fraud.service.url:http://fraud-service:8086}")
    private String fraudServiceUrl;

    public FraudResult checkFraud(FraudCheckRequest request) {
        String url = fraudServiceUrl + "/api/v1/fraud/check";
        try {
            log.info("Calling Fraud Service at: {}", url);
            ResponseEntity<ApiResponse<FraudResult>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<FraudResult>>() {
                    });
            return response.getBody().getData();
        } catch (Exception e) {
            log.error("Failed to call Fraud Service", e);
            // Fail-open or Fail-closed policy?
            // For safety, we return a fallback "Manual Review" result to not block but flag
            return FraudResult.builder()
                    .decision(FraudResult.FraudDecision.MANUAL_REVIEW)
                    .riskScore(0.5)
                    .transactionId(request.getTransactionId())
                    .build();
        }
    }
}
