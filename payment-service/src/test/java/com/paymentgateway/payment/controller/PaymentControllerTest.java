package com.paymentgateway.payment.controller;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.common.model.Transaction;
import com.paymentgateway.payment.dto.PaymentRequest;
import com.paymentgateway.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void processPayment_ReturnsSuccess() {
        PaymentRequest request = new PaymentRequest();
        Transaction transaction = new Transaction();
        transaction.setId("tx-1");

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(transaction);

        ResponseEntity<ApiResponse<Transaction>> response = paymentController.processPayment(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(transaction);
    }

    @Test
    void getTransaction_ReturnsSuccess() {
        Transaction transaction = new Transaction();
        transaction.setId("tx-1");

        when(paymentService.getTransaction("tx-1")).thenReturn(transaction);

        ResponseEntity<ApiResponse<Transaction>> response = paymentController.getTransaction("tx-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEqualTo(transaction);
    }

    @Test
    void getMerchantTransactions_ReturnsSuccess() {
        List<Transaction> transactions = List.of(new Transaction());
        when(paymentService.getMerchantTransactions("m-1")).thenReturn(transactions);

        ResponseEntity<ApiResponse<List<Transaction>>> response = paymentController.getMerchantTransactions("m-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    void capturePayment_ReturnsSuccess() {
        Transaction transaction = new Transaction();
        transaction.setId("tx-1");

        when(paymentService.capturePayment("tx-1")).thenReturn(transaction);

        ResponseEntity<ApiResponse<Transaction>> response = paymentController.capturePayment("tx-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEqualTo(transaction);
    }
}
