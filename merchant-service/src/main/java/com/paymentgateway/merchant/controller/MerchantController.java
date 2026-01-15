package com.paymentgateway.merchant.controller;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.common.dto.merchant.MerchantRequest;
import com.paymentgateway.common.dto.merchant.MerchantResponse;
import com.paymentgateway.merchant.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping
    public ResponseEntity<ApiResponse<MerchantResponse>> createMerchant(@RequestBody MerchantRequest request) {
        log.info("Creating merchant: {}", request.getName());
        return ResponseEntity
                .ok(ApiResponse.success("Merchant created successfully", merchantService.createMerchant(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MerchantResponse>> getMerchant(@PathVariable("id") String id) {
        log.info("Fetching merchant: {}", id);
        return ResponseEntity.ok(ApiResponse.success(merchantService.getMerchant(id)));
    }
}
