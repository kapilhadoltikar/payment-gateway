package com.paymentgateway.gateway.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class GatewayExceptionHandlerTest {

    @Test
    void gatewayExceptionHandler_Instantiation() {
        GatewayExceptionHandler handler = new GatewayExceptionHandler();
        assertThat(handler).isNotNull();
        // Since it inherits from ReactiveBaseExceptionHandler which we already tested
        // in common,
        // we just ensure it exists and can be instantiated.
    }
}
