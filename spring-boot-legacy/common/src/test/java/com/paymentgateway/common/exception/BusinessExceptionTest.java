package com.paymentgateway.common.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class BusinessExceptionTest {

    @Test
    void constructors_WorkCorrectly() {
        BusinessException ex1 = new BusinessException("Message 1");
        assertThat(ex1.getMessage()).isEqualTo("Message 1");
        assertThat(ex1.getErrorCode()).isEqualTo("BUSINESS_ERROR");
        assertThat(ex1.getHttpStatus()).isEqualTo(400);

        BusinessException ex2 = new BusinessException("Message 2", "ERR_CODE");
        assertThat(ex2.getMessage()).isEqualTo("Message 2");
        assertThat(ex2.getErrorCode()).isEqualTo("ERR_CODE");
        assertThat(ex2.getHttpStatus()).isEqualTo(400);

        BusinessException ex3 = new BusinessException("Message 3", "ERR_CODE", 500);
        assertThat(ex3.getMessage()).isEqualTo("Message 3");
        assertThat(ex3.getErrorCode()).isEqualTo("ERR_CODE");
        assertThat(ex3.getHttpStatus()).isEqualTo(500);

        Exception cause = new Exception("Root cause");
        BusinessException ex4 = new BusinessException("Message 4", cause, "ERR_CODE", 403);
        assertThat(ex4.getMessage()).isEqualTo("Message 4");
        assertThat(ex4.getCause()).isEqualTo(cause);
        assertThat(ex4.getErrorCode()).isEqualTo("ERR_CODE");
        assertThat(ex4.getHttpStatus()).isEqualTo(403);
    }
}
