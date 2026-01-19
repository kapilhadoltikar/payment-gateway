package com.paymentgateway.fraud.controller;

import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.dto.FraudResult;
import com.paymentgateway.fraud.service.FraudDetectionService;
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
    public Mono<FraudResult> checkFraud(@RequestBody FraudCheckRequest request) {
        return fraudService.evaluateRisk(request);
    }
}
