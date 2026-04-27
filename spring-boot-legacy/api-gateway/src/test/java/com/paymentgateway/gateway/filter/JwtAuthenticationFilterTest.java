package com.paymentgateway.gateway.filter;

import com.paymentgateway.common.config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

        @Mock
        private JwtUtil jwtUtil;

        @Mock
        private GatewayFilterChain chain;

        private JwtAuthenticationFilter filter;

        @BeforeEach
        void setUp() {
                filter = new JwtAuthenticationFilter(jwtUtil);
        }

        @Test
        void apply_WhitelistPath_Success() {
                MockServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/v3/api-docs").build());

                when(chain.filter(any())).thenReturn(Mono.empty());

                filter.apply(new JwtAuthenticationFilter.Config()).filter(exchange, chain).block();

                verify(chain).filter(exchange);
                verifyNoInteractions(jwtUtil);
        }

        @Test
        void apply_NoAuthHeader_ReturnsUnauthorized() {
                MockServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/api/v1/payments").build());

                Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config()).filter(exchange, chain);

                StepVerifier.create(result).verifyComplete();
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                verifyNoInteractions(chain);
        }

        @Test
        void apply_InvalidAuthHeader_ReturnsUnauthorized() {
                MockServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/api/v1/payments")
                                                .header(HttpHeaders.AUTHORIZATION, "Basic someauth")
                                                .build());

                Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config()).filter(exchange, chain);

                StepVerifier.create(result).verifyComplete();
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                verifyNoInteractions(chain);
        }

        @Test
        void apply_RecruiterDemoToken_Success() {
                MockServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/api/v1/payments")
                                                .header(HttpHeaders.AUTHORIZATION, "Bearer RECRUITER_DEMO_2026")
                                                .build());

                when(chain.filter(any())).thenReturn(Mono.empty());

                filter.apply(new JwtAuthenticationFilter.Config()).filter(exchange, chain).block();

                ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
                verify(chain).filter(exchangeCaptor.capture());

                ServerWebExchange capturedExchange = exchangeCaptor.getValue();
                assertThat(capturedExchange.getRequest().getHeaders().getFirst("X-Auth-User"))
                                .isEqualTo("recruiter-demo");
        }

        @Test
        void apply_ValidToken_Success() {
                String token = "valid-token";
                MockServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/api/v1/payments")
                                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                                .build());

                when(jwtUtil.validateToken(token)).thenReturn(true);
                when(jwtUtil.extractUsername(token)).thenReturn("user1");
                when(chain.filter(any())).thenReturn(Mono.empty());

                filter.apply(new JwtAuthenticationFilter.Config()).filter(exchange, chain).block();

                ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
                verify(chain).filter(exchangeCaptor.capture());

                ServerWebExchange capturedExchange = exchangeCaptor.getValue();
                assertThat(capturedExchange.getRequest().getHeaders().getFirst("X-Auth-User")).isEqualTo("user1");
        }

        @Test
        void apply_InvalidToken_ReturnsUnauthorized() {
                String token = "invalid-token";
                MockServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/api/v1/payments")
                                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                                .build());

                when(jwtUtil.validateToken(token)).thenReturn(false);

                Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config()).filter(exchange, chain);

                StepVerifier.create(result).verifyComplete();
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                verifyNoInteractions(chain);
        }

        @Test
        void apply_TokenValidationException_ReturnsUnauthorized() {
                String token = "error-token";
                MockServerWebExchange exchange = MockServerWebExchange.from(
                                MockServerHttpRequest.get("/api/v1/payments")
                                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                                .build());

                when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("Token error"));

                Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config()).filter(exchange, chain);

                StepVerifier.create(result).verifyComplete();
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                verifyNoInteractions(chain);
        }
}
