package com.paymentgateway.vault.controller;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.common.dto.vault.CardDataResponse;
import com.paymentgateway.common.dto.vault.TokenizeRequest;
import com.paymentgateway.common.dto.vault.TokenizeResponse;
import com.paymentgateway.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/vault")
@RequiredArgsConstructor
public class VaultController {

    private final VaultService vaultService;

    @PostMapping("/tokenize")
    public ResponseEntity<ApiResponse<TokenizeResponse>> tokenize(@RequestBody TokenizeRequest request) {
        log.info("Tokenizing card data for holder: {}", request.getCardHolderName());
        return ResponseEntity.ok(ApiResponse.success("Card tokenized successfully", vaultService.tokenize(request)));
    }

    @GetMapping("/detokenize/{token}")
    public ResponseEntity<ApiResponse<CardDataResponse>> detokenize(@PathVariable("token") String token) {
        log.info("Detokenizing token: {}", token);
        return ResponseEntity.ok(ApiResponse.success(vaultService.detokenize(token)));
    }
}
