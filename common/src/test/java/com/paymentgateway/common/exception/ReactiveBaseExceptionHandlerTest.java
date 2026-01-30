package com.paymentgateway.common.exception;

import com.paymentgateway.common.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

public class ReactiveBaseExceptionHandlerTest {

    private final TestReactiveHandler handler = new TestReactiveHandler();

    @Test
    void handleBusinessException_ReturnsMonoWithCorrectResponse() {
        BusinessException ex = new BusinessException("Biz error", "BIZ_001", 400);
        Mono<ResponseEntity<ApiResponse<Void>>> result = handler.handleBusinessException(ex);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody().getMessage()).isEqualTo("Biz error");
                    assertThat(response.getBody().getErrorCode()).isEqualTo("BIZ_001");
                })
                .verifyComplete();
    }

    @Test
    void handleGeneralException_ReturnsMonoWithCorrectResponse() {
        Exception ex = new Exception("General error");
        Mono<ResponseEntity<ApiResponse<Void>>> result = handler.handleGeneralException(ex);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
                    assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
                })
                .verifyComplete();
    }

    private static class TestReactiveHandler extends ReactiveBaseExceptionHandler {
    }
}
