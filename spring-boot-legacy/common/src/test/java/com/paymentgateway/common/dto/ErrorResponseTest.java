package com.paymentgateway.common.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class ErrorResponseTest {

    @Test
    void builderAndAccessors_WorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse.FieldError fieldError = ErrorResponse.FieldError.builder()
                .field("amount")
                .message("must be positive")
                .rejectedValue(-10)
                .build();

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VAL_ERR")
                .message("Validation failed")
                .timestamp(now)
                .path("/api/payment")
                .fieldErrors(List.of(fieldError))
                .build();

        assertThat(response.getErrorCode()).isEqualTo("VAL_ERR");
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.getPath()).isEqualTo("/api/payment");
        assertThat(response.getFieldErrors()).hasSize(1);

        ErrorResponse.FieldError fe = response.getFieldErrors().get(0);
        assertThat(fe.getField()).isEqualTo("amount");
        assertThat(fe.getMessage()).isEqualTo("must be positive");
        assertThat(fe.getRejectedValue()).isEqualTo(-10);
    }

    @Test
    void noArgsConstructor_WorksCorrectly() {
        ErrorResponse response = new ErrorResponse();
        assertThat(response.getErrorCode()).isNull();

        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError();
        assertThat(fieldError.getField()).isNull();
    }
}
