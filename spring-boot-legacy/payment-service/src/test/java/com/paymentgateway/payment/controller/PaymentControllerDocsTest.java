package com.paymentgateway.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.payment.dto.PaymentRequest;
import com.paymentgateway.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@WebMvcTest(PaymentController.class)
public class PaymentControllerDocsTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private PaymentService paymentService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        public void setUp(WebApplicationContext webApplicationContext,
                        RestDocumentationContextProvider restDocumentation) {
                this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(documentationConfiguration(restDocumentation))
                                .build();
        }

        @Test
        public void processPayment() throws Exception {
                PaymentRequest request = new PaymentRequest();
                request.setMerchantId("MERCH-123");
                request.setAmount(new BigDecimal("100.00"));
                request.setCurrency("USD");
                request.setPaymentMethod("CARD");
                request.setCardNumber("4111222233334444");
                request.setExpiryMonth("12");
                request.setExpiryYear("2026");
                request.setCvv("123");
                request.setCardHolderName("John Doe");
                request.setCustomerEmail("john@example.com");

                this.mockMvc.perform(post("/payments/process")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(this.objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andDo(document("process-payment",
                                                requestFields(
                                                                fieldWithPath("merchantId")
                                                                                .description("The ID of the merchant"),
                                                                fieldWithPath("amount").description(
                                                                                "The amount of the transaction"),
                                                                fieldWithPath("currency").description(
                                                                                "The currency of the transaction (e.g., USD)"),
                                                                fieldWithPath("paymentMethod").description(
                                                                                "The payment method (CARD, UPI, etc.)"),
                                                                fieldWithPath("cardToken")
                                                                                .description("Optional card token")
                                                                                .optional(),
                                                                fieldWithPath("cardNumber")
                                                                                .description("The credit card number")
                                                                                .optional(),
                                                                fieldWithPath("expiryMonth").description(
                                                                                "The expiry month of the card")
                                                                                .optional(),
                                                                fieldWithPath("expiryYear").description(
                                                                                "The expiry year of the card")
                                                                                .optional(),
                                                                fieldWithPath("cvv")
                                                                                .description("The CVV code of the card")
                                                                                .optional(),
                                                                fieldWithPath("cardHolderName").description(
                                                                                "The name of the card holder")
                                                                                .optional(),
                                                                fieldWithPath("customerEmail").description(
                                                                                "The email of the customer"),
                                                                fieldWithPath("description").description(
                                                                                "Optional description of the payment")
                                                                                .optional(),
                                                                fieldWithPath("idempotencyKey")
                                                                                .description("Optional idempotency key")
                                                                                .optional()),
                                                responseFields(
                                                                fieldWithPath("success").description(
                                                                                "Whether the operation was successful"),
                                                                fieldWithPath("message").description(
                                                                                "A message describing the result"),
                                                                fieldWithPath("data").description(
                                                                                "The transaction details (if successful)")
                                                                                .optional(),
                                                                fieldWithPath("errorCode").description(
                                                                                "The error code if the operation failed")
                                                                                .optional(),
                                                                fieldWithPath("timestamp").description(
                                                                                "The timestamp of the response"))));
        }
}
