package com.paymentgateway.common.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiResponseTest {

    @Test
    void success_WithData_SetsFieldsCorrectly() {
        String data = "test-data";
        ApiResponse<String> response = ApiResponse.success(data);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(response.getMessage()).isNull();
    }

    @Test
    void success_WithMessageAndData_SetsFieldsCorrectly() {
        String data = "test-data";
        String message = "Operation successful";
        ApiResponse<String> response = ApiResponse.success(message, data);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void error_SetsFieldsCorrectly() {
        String message = "Error occurred";
        String errorCode = "ERR_001";
        ApiResponse<Void> response = ApiResponse.error(message, errorCode);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void builder_WorksCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Built")
                .data("Data")
                .errorCode("NONE")
                .timestamp(now)
                .build();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Built");
        assertThat(response.getData()).isEqualTo("Data");
        assertThat(response.getErrorCode()).isEqualTo("NONE");
        assertThat(response.getTimestamp()).isEqualTo(now);
    }

    @Test
    void setterAndGetter_WorksCorrectly() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("Msg");
        response.setData("Val");
        response.setErrorCode("ERR");
        LocalDateTime now = LocalDateTime.now();
        response.setTimestamp(now);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Msg");
        assertThat(response.getData()).isEqualTo("Val");
        assertThat(response.getErrorCode()).isEqualTo("ERR");
        assertThat(response.getTimestamp()).isEqualTo(now);
    }
}
