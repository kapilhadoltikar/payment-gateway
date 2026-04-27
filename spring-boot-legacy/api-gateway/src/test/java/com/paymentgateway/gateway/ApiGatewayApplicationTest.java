package com.paymentgateway.gateway;

import com.paymentgateway.common.config.JwtUtil;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ApiGatewayApplicationTest {

    @Test
    void applicationBeanCreation() {
        ApiGatewayApplication application = new ApiGatewayApplication();
        JwtUtil jwtUtil = application.jwtUtil();
        assertThat(jwtUtil).isNotNull();
    }
}
