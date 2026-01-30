package com.paymentgateway.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayConfigTest {

    @Test
    void rateLimiterConfig_UserKeyResolver_AuthHeaderPresent() {
        RateLimiterConfig config = new RateLimiterConfig();
        KeyResolver resolver = config.userKeyResolver();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .build());

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("Bearer test-token")
                .verifyComplete();
    }

    @Test
    void rateLimiterConfig_UserKeyResolver_FallbackToIp() {
        RateLimiterConfig config = new RateLimiterConfig();
        KeyResolver resolver = config.userKeyResolver();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/")
                        .remoteAddress(new InetSocketAddress("192.168.1.1", 8080))
                        .build());

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("192.168.1.1")
                .verifyComplete();
    }

    @Test
    void rateLimiterConfig_IpKeyResolver() {
        RateLimiterConfig config = new RateLimiterConfig();
        KeyResolver resolver = config.ipKeyResolver();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/")
                        .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                        .build());

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("10.0.0.1")
                .verifyComplete();
    }

    @Test
    void openApiConfig_Instantiation() {
        OpenApiConfig config = new OpenApiConfig();
        assertThat(config).isNotNull();
    }
}
