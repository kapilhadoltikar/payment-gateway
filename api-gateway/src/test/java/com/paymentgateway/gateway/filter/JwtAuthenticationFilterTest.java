package com.paymentgateway.gateway.filter;

import com.paymentgateway.common.config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

        @Mock
        private JwtUtil jwtUtil;

        @Mock
        private GatewayFilterChain chain;

        private JwtAuthenticationFilter filter;

        @BeforeEach
        public void setUp() {
                filter = new JwtAuthenticationFilter(jwtUtil);
        }

        @Test
        public void filter_WithNoAuthHeader_ReturnsUnauthorized() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);

                GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
                Mono<Void> result = gatewayFilter.filter(exchange, chain);

                StepVerifier.create(result)
                                .verifyComplete();

                assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
                verify(chain, never()).filter(any());
        }

        @Test
        public void filter_WithInvalidAuthHeader_ReturnsUnauthorized() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                                .header(HttpHeaders.AUTHORIZATION, "InvalidBearer token")
                                .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);

                GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
                Mono<Void> result = gatewayFilter.filter(exchange, chain);

                StepVerifier.create(result)
                                .verifyComplete();

                assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
                verify(chain, never()).filter(any());
        }

        @Test
        public void filter_WithInvalidToken_ReturnsUnauthorized() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                                .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);
                when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

                GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
                Mono<Void> result = gatewayFilter.filter(exchange, chain);

                StepVerifier.create(result)
                                .verifyComplete();

                assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
                verify(chain, never()).filter(any());
        }

        @Test
        public void filter_WithValidToken_AppliesFilter() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                                .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);

                when(jwtUtil.validateToken("valid-token")).thenReturn(true);
                when(jwtUtil.extractUsername("valid-token")).thenReturn("testuser");
                when(chain.filter(any())).thenReturn(Mono.empty());

                GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
                Mono<Void> result = gatewayFilter.filter(exchange, chain);

                StepVerifier.create(result)
                                .verifyComplete();

                ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
                verify(chain).filter(captor.capture());
                ServerWebExchange mutatedExchange = captor.getValue();
                assertEquals("testuser", mutatedExchange.getRequest().getHeaders().getFirst("X-Auth-User"));
        }
}
