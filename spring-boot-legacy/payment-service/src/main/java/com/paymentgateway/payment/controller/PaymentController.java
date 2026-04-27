package com.paymentgateway.payment.controller;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.payment.dto.PaymentRequest;
import com.paymentgateway.common.model.Transaction;
import com.paymentgateway.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payment REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<Transaction>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request for merchant: {}", request.getMerchantId());
        Transaction transaction = paymentService.processPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", transaction));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<Transaction>> getTransaction(
            @PathVariable("transactionId") String transactionId) {
        Transaction transaction = paymentService.getTransaction(transactionId);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<ApiResponse<List<Transaction>>> getMerchantTransactions(
            @PathVariable("merchantId") String merchantId) {
        List<Transaction> transactions = paymentService.getMerchantTransactions(merchantId);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @PostMapping("/{transactionId}/capture")
    public ResponseEntity<ApiResponse<Transaction>> capturePayment(
            @PathVariable("transactionId") String transactionId) {
        Transaction transaction = paymentService.capturePayment(transactionId);
        return ResponseEntity.ok(ApiResponse.success("Payment captured successfully", transaction));
    }
}
