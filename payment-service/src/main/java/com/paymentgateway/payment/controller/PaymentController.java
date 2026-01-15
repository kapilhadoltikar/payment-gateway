package com.paymentgateway.payment.controller;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.payment.dto.PaymentRequest;
import com.paymentgateway.common.model.Transaction;
import com.paymentgateway.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

        try {
            Transaction transaction = paymentService.processPayment(request);
            return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", transaction));
        } catch (Exception e) {
            log.error("Payment processing failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "PAYMENT_ERROR"));
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<Transaction>> getTransaction(
            @PathVariable("transactionId") String transactionId) {
        try {
            Transaction transaction = paymentService.getTransaction(transactionId);
            return ResponseEntity.ok(ApiResponse.success(transaction));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        }
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
        try {
            Transaction transaction = paymentService.capturePayment(transactionId);
            return ResponseEntity.ok(ApiResponse.success("Payment captured successfully", transaction));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "CAPTURE_ERROR"));
        }
    }
}
