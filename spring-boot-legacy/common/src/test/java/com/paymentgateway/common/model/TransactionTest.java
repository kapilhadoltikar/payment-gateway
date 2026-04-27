package com.paymentgateway.common.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionTest {

    @Test
    void builderAndAccessors_WorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = Transaction.builder()
                .id("tx-123")
                .merchantId("m-456")
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .status(Transaction.TransactionStatus.INITIATED)
                .paymentMethod(Transaction.PaymentMethod.CARD)
                .cardToken("tok-789")
                .authorizationCode("auth-000")
                .referenceNumber("ref-111")
                .description("Test transaction")
                .customerEmail("test@example.com")
                .failureReason("None")
                .idempotencyKey("idem-key")
                .createdAt(now)
                .updatedAt(now)
                .settledAt(now)
                .build();

        assertThat(transaction.getId()).isEqualTo("tx-123");
        assertThat(transaction.getMerchantId()).isEqualTo("m-456");
        assertThat(transaction.getAmount()).isEqualTo(new BigDecimal("99.99"));
        assertThat(transaction.getCurrency()).isEqualTo("USD");
        assertThat(transaction.getStatus()).isEqualTo(Transaction.TransactionStatus.INITIATED);
        assertThat(transaction.getPaymentMethod()).isEqualTo(Transaction.PaymentMethod.CARD);
        assertThat(transaction.getCardToken()).isEqualTo("tok-789");
        assertThat(transaction.getAuthorizationCode()).isEqualTo("auth-000");
        assertThat(transaction.getReferenceNumber()).isEqualTo("ref-111");
        assertThat(transaction.getDescription()).isEqualTo("Test transaction");
        assertThat(transaction.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(transaction.getFailureReason()).isEqualTo("None");
        assertThat(transaction.getIdempotencyKey()).isEqualTo("idem-key");
        assertThat(transaction.getCreatedAt()).isEqualTo(now);
        assertThat(transaction.getUpdatedAt()).isEqualTo(now);
        assertThat(transaction.getSettledAt()).isEqualTo(now);
    }

    @Test
    void noArgsConstructor_WorksCorrectly() {
        Transaction transaction = new Transaction();
        transaction.setId("tx-1");
        assertThat(transaction.getId()).isEqualTo("tx-1");
    }
}
