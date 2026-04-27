package com.paymentgateway.fraud.controller;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.dto.FraudResult;
import com.paymentgateway.fraud.service.FraudDetectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudDetectionService fraudService;

    @PostMapping("/check")
    public Mono<ApiResponse<FraudResult>> checkFraud(@Valid @RequestBody FraudCheckRequest request) {
        return fraudService.evaluateRisk(request)
                .map(result -> ApiResponse.success("Fraud check completed", result));
    }
}
