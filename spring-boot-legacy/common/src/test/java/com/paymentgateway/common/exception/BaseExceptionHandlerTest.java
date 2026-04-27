package com.paymentgateway.common.exception;

import com.paymentgateway.common.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseExceptionHandlerTest {

    private final TestHandler handler = new TestHandler();

    @Test
    void handleBusinessException_ReturnsCorrectResponse() {
        BusinessException ex = new BusinessException("Biz error", "BIZ_001", 400);
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage()).isEqualTo("Biz error");
        assertThat(response.getBody().getErrorCode()).isEqualTo("BIZ_001");
    }

    @Test
    void handleGeneralException_ReturnsCorrectResponse() {
        Exception ex = new Exception("General error");
        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneralException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    private static class TestHandler extends BaseExceptionHandler {
    }
}
