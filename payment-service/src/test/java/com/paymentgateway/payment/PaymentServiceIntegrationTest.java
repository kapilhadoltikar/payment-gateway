package com.paymentgateway.payment;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.common.dto.merchant.MerchantResponse;
import com.paymentgateway.common.dto.vault.TokenizeResponse;
import com.paymentgateway.common.model.Transaction;
import com.paymentgateway.fraud.dto.FraudResult;
import com.paymentgateway.fraud.dto.FraudResult.FraudDecision;
import com.paymentgateway.payment.dto.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = PaymentServiceApplication.class, properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "spring.kafka.bootstrap-servers=localhost:9092"
})
@ActiveProfiles("test")
public class PaymentServiceIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @MockitoBean
        private com.paymentgateway.payment.repository.TransactionRepository transactionRepository;

        @MockitoBean
        private com.paymentgateway.payment.client.VaultClient vaultClient;

        @MockitoBean
        private com.paymentgateway.payment.client.MerchantClient merchantClient;

        @MockitoBean
        private com.paymentgateway.payment.client.FraudClient fraudClient;

        @MockitoBean
        private com.paymentgateway.payment.messaging.KafkaProducer kafkaProducer;

        @BeforeEach
        void setup() {
                // Mock TransactionRepository
                Mockito.when(transactionRepository.save(ArgumentMatchers.any()))
                                .thenAnswer(invocation -> {
                                        Transaction tx = invocation.getArgument(0);
                                        if (tx.getId() == null) {
                                                java.lang.reflect.Field field = tx.getClass().getDeclaredField("id");
                                                field.setAccessible(true);
                                                field.set(tx, java.util.UUID.randomUUID().toString());
                                        }
                                        return tx;
                                });

                // Mock MerchantClient
                Mockito.when(merchantClient.getMerchant((String) ArgumentMatchers.anyString()))
                                .thenReturn(new MerchantResponse());

                // Mock VaultClient
                Mockito.when(vaultClient.tokenize(ArgumentMatchers.any()))
                                .thenReturn(TokenizeResponse.builder()
                                                .token("mock-token-" + java.util.UUID.randomUUID())
                                                .build());

                // Mock FraudClient
                Mockito.when(fraudClient.checkFraud(ArgumentMatchers.any()))
                                .thenReturn(FraudResult.builder()
                                                .decision(FraudDecision.APPROVE)
                                                .riskScore(0.1)
                                                .build());
        }

        @Test
        void shouldProcessPaymentSuccessfully() {
                // Given
                PaymentRequest request = new PaymentRequest();
                request.setAmount(new java.math.BigDecimal("100.00"));
                request.setCurrency("USD");
                request.setMerchantId("MERCHANT-001");
                request.setPaymentMethod("CARD");
                request.setCardNumber("4111111111111111");
                request.setExpiryMonth("12");
                request.setExpiryYear("2030");
                request.setCardHolderName("John Doe");
                request.setCvv("123");
                request.setCustomerEmail("john@example.com");

                // When
                ResponseEntity<ApiResponse<com.paymentgateway.common.model.Transaction>> response = restTemplate
                                .exchange(
                                                "/payments/process",
                                                org.springframework.http.HttpMethod.POST,
                                                new org.springframework.http.HttpEntity<>(request),
                                                new org.springframework.core.ParameterizedTypeReference<ApiResponse<com.paymentgateway.common.model.Transaction>>() {
                                                });

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().isSuccess()).isTrue();
        }
}
