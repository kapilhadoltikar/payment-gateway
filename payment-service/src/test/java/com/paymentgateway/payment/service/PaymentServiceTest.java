package com.paymentgateway.payment.service;

import com.paymentgateway.common.dto.merchant.MerchantResponse;
import com.paymentgateway.common.dto.vault.TokenizeRequest;
import com.paymentgateway.common.dto.vault.TokenizeResponse;
import com.paymentgateway.common.model.Transaction;
import com.paymentgateway.payment.client.FraudClient;
import com.paymentgateway.payment.client.MerchantClient;
import com.paymentgateway.payment.client.VaultClient;
import com.paymentgateway.payment.dto.PaymentRequest;
import com.paymentgateway.common.exception.BusinessException;
import com.paymentgateway.common.exception.ValidationException;
import com.paymentgateway.payment.messaging.KafkaProducer;
import com.paymentgateway.payment.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private VaultClient vaultClient;

    @Mock
    private MerchantClient merchantClient;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private FraudClient fraudClient;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPayment_Success() {
        // Arrange
        PaymentRequest request = createValidRequest();
        when(merchantClient.getMerchant(any())).thenReturn(new MerchantResponse());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(UUID.randomUUID().toString());
            return t;
        });
        when(fraudClient.checkFraud(any())).thenReturn(com.paymentgateway.fraud.dto.FraudResult.builder()
                .decision(com.paymentgateway.fraud.dto.FraudResult.FraudDecision.APPROVE)
                .build());

        // Act
        Transaction result = paymentService.processPayment(request);

        // Assert
        assertNotNull(result.getId());
        assertEquals(Transaction.TransactionStatus.AUTHORIZED, result.getStatus());
        verify(kafkaProducer).sendPaymentEvent(any());
        verify(transactionRepository, atLeast(2)).save(any());
    }

    @Test
    void processPayment_IdempotencyValues() {
        // Arrange
        String idempotencyKey = "key-123";
        PaymentRequest request = createValidRequest();
        request.setIdempotencyKey(idempotencyKey);

        Transaction existing = Transaction.builder()
                .id("existing-id")
                .idempotencyKey(idempotencyKey)
                .status(Transaction.TransactionStatus.AUTHORIZED)
                .build();

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existing));

        // Act
        Transaction result = paymentService.processPayment(request);

        // Assert
        assertEquals("existing-id", result.getId());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processPayment_FraudBlock() {
        // Arrange
        PaymentRequest request = createValidRequest();
        when(merchantClient.getMerchant(any())).thenReturn(new MerchantResponse());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(fraudClient.checkFraud(any())).thenReturn(com.paymentgateway.fraud.dto.FraudResult.builder()
                .decision(com.paymentgateway.fraud.dto.FraudResult.FraudDecision.BLOCK)
                .riskScore(0.9)
                .build());

        // Act
        Transaction result = paymentService.processPayment(request);

        // Assert
        assertEquals(Transaction.TransactionStatus.FAILED, result.getStatus());
        assertEquals("High Risk Fraud Detected", result.getFailureReason());
    }

    @Test
    void processPayment_InvalidCurrency() {
        PaymentRequest request = createValidRequest();
        request.setCurrency("US"); // Invalid

        assertThrows(ValidationException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void processPayment_MerchantNotFound() {
        // Arrange
        PaymentRequest request = createValidRequest();
        when(merchantClient.getMerchant(any())).thenThrow(new RuntimeException("Service Down"));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.processPayment(request));
        assertEquals("MERCHANT_NOT_FOUND", ex.getErrorCode());
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void processPayment_VaultFailure() {
        // Arrange
        PaymentRequest request = createValidRequest();
        request.setCardToken(null);
        request.setCardNumber("1234567890123456");
        request.setExpiryMonth("12");
        request.setExpiryYear("2025");

        when(merchantClient.getMerchant(any())).thenReturn(new MerchantResponse());
        when(vaultClient.tokenize(any())).thenThrow(new RuntimeException("Vault Down"));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.processPayment(request));
        assertEquals("VAULT_ERROR", ex.getErrorCode());
        assertEquals(500, ex.getHttpStatus());
    }

    private PaymentRequest createValidRequest() {
        return PaymentRequest.builder()
                .merchantId("mer_123")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .paymentMethod("CARD")
                .cardToken("tok_123")
                .customerEmail("test@example.com")
                .build();
    }
}
