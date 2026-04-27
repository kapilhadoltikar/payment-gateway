package com.paymentgateway.payment.exception;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.common.exception.BusinessException;
import com.paymentgateway.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentExceptionHandlerTest {

    private final PaymentExceptionHandler exceptionHandler = new PaymentExceptionHandler();

    @Test
    void handleBusinessException_ReturnsCorrectResponse() {
        BusinessException ex = new BusinessException("Payment failed", "PAYMENT_FAILED", 400);
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage()).isEqualTo("Payment failed");
        assertThat(response.getBody().getErrorCode()).isEqualTo("PAYMENT_FAILED");
    }

    @Test
    void handleGeneralException_ReturnsCorrectResponse() {
        Exception ex = new Exception("Internal error");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGeneralException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    void handleValidationException_ReturnsCorrectResponse() {
        ValidationException ex = new ValidationException("Invalid input");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage()).isEqualTo("Invalid input");
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }
}
