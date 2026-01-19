package com.paymentgateway.payment.client;

import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.dto.FraudResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
            return restTemplate.postForObject(url, request, FraudResult.class);
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
