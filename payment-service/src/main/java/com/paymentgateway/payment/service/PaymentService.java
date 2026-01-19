package com.paymentgateway.payment.service;

import com.paymentgateway.common.dto.vault.TokenizeRequest;
import com.paymentgateway.common.dto.vault.TokenizeResponse;
import com.paymentgateway.common.model.Transaction;
import com.paymentgateway.payment.client.MerchantClient;
import com.paymentgateway.payment.client.VaultClient;
import com.paymentgateway.payment.dto.PaymentRequest;
import com.paymentgateway.payment.messaging.KafkaProducer;
import com.paymentgateway.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final VaultClient vaultClient;
    private final MerchantClient merchantClient;
    private final KafkaProducer kafkaProducer;
    private final com.paymentgateway.payment.client.FraudClient fraudClient;

    @Transactional
    public Transaction processPayment(PaymentRequest request) {
        log.info("Processing payment for merchant: {}", request.getMerchantId());

        // 1. Validate Merchant
        try {
            merchantClient.getMerchant(request.getMerchantId());
        } catch (Exception e) {
            log.error("Merchant validation failed: {}", request.getMerchantId());
            throw new RuntimeException("Invalid merchant ID provided");
        }

        // 2. Tokenize Card
        String cardToken = request.getCardToken();
        if (cardToken == null && request.getCardNumber() != null) {
            TokenizeRequest tokenizeRequest = TokenizeRequest.builder()
                    .pan(request.getCardNumber())
                    .expiryDate(request.getExpiryMonth() + "/" + request.getExpiryYear())
                    .cardHolderName(request.getCardHolderName())
                    .cvv(request.getCvv())
                    .build();
            try {
                TokenizeResponse tokenizeResponse = vaultClient.tokenize(tokenizeRequest);
                cardToken = tokenizeResponse.getToken();
            } catch (Exception e) {
                log.error("Vault tokenization failed", e);
                throw new RuntimeException("Secure tokenization failed: " + e.getMessage());
            }
        }

        // 3. Create Local Transaction record
        Transaction transaction = Transaction.builder()
                .merchantId(request.getMerchantId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(Transaction.TransactionStatus.INITIATED)
                .paymentMethod(Transaction.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()))
                .cardToken(cardToken)
                .customerEmail(request.getCustomerEmail())
                .description(request.getDescription())
                .build();

        log.info("Saving Transaction: merchantId={}, amount={}, cardToken={}",
                transaction.getMerchantId(), transaction.getAmount(), transaction.getCardToken());

        transaction = transactionRepository.save(transaction);

        // 4. Fraud Detection Check
        try {
            com.paymentgateway.fraud.dto.FraudCheckRequest fraudRequest = com.paymentgateway.fraud.dto.FraudCheckRequest
                    .builder()
                    .transactionId(transaction.getId())
                    .merchantId(request.getMerchantId())
                    .userId(request.getCustomerEmail()) // Using email as userId for now
                    .amount(request.getAmount().doubleValue())
                    .currency(request.getCurrency())
                    .build();

            com.paymentgateway.fraud.dto.FraudResult fraudResult = fraudClient.checkFraud(fraudRequest);

            if (fraudResult.getDecision() == com.paymentgateway.fraud.dto.FraudResult.FraudDecision.BLOCK) {
                log.warn("Transaction blocked by Fraud Service. Risk Score: {}", fraudResult.getRiskScore());
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transaction.setFailureReason("High Risk Fraud Detected");
                transactionRepository.save(transaction);
                kafkaProducer.sendPaymentEvent(transaction);
                return transaction;
            } else if (fraudResult
                    .getDecision() == com.paymentgateway.fraud.dto.FraudResult.FraudDecision.MANUAL_REVIEW) {
                log.info("Transaction flagged for Manual Review (Gray Path). Risk Score: {}",
                        fraudResult.getRiskScore());
                // For demo, we designate this by modifying the description or log, but proceed
                // to auth
                transaction.setDescription(transaction.getDescription() + " [REVIEW REQUIRED]");
            }

        } catch (Exception e) {
            log.error("Fraud check failed, failing open (allowing transaction) but logging error", e);
        }

        // 5. Process Path
        Transaction modernResult = processModernPath(transaction);

        return modernResult;
    }

    private Transaction processModernPath(Transaction transaction) {
        log.info("[MODERN-CORE] Executing modern authorization logic");
        if (transaction.getAmount().doubleValue() < 10000.0) {
            transaction.setStatus(Transaction.TransactionStatus.AUTHORIZED);
            transaction.setAuthorizationCode("AUTH_" + UUID.randomUUID().toString().substring(0, 8));
            transaction.setReferenceNumber("REF_" + System.currentTimeMillis());
        } else {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason("Business logic limit exceeded");
        }
        transaction = transactionRepository.save(transaction);
        kafkaProducer.sendPaymentEvent(transaction);
        return transaction;
    }

    public Transaction getTransaction(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public List<Transaction> getMerchantTransactions(String merchantId) {
        return transactionRepository.findByMerchantId(merchantId);
    }

    @Transactional
    public Transaction capturePayment(String transactionId) {
        Transaction transaction = getTransaction(transactionId);
        if (transaction.getStatus() != Transaction.TransactionStatus.AUTHORIZED) {
            throw new RuntimeException("Cannot capture unauthorized transaction");
        }

        transaction.setStatus(Transaction.TransactionStatus.CAPTURED);
        transaction.setSettledAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        kafkaProducer.sendPaymentEvent(transaction);
        return transaction;
    }
}
