package com.paymentgateway.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void securityConfig_ReturnsPasswordEncoder() {
        SecurityConfig config = new SecurityConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        assertThat(encoder).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void securityConfig_FilterChainWorks() throws Exception {
        SecurityConfig config = new SecurityConfig();
        HttpSecurity http = mock(HttpSecurity.class);

        when(http.csrf(any())).thenReturn(http);

        when(http.authorizeHttpRequests(any())).thenAnswer(invocation -> {
            Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> customizer = invocation
                    .getArgument(0);

            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry = mock(
                    AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl = mock(
                    AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);

            when(registry.requestMatchers(any(String[].class))).thenReturn(authorizedUrl);
            when(authorizedUrl.permitAll()).thenReturn(registry);
            when(registry.anyRequest()).thenReturn(authorizedUrl);
            when(authorizedUrl.authenticated()).thenReturn(registry);

            customizer.customize(registry);
            return http;
        });

        DefaultSecurityFilterChain dsfc = new DefaultSecurityFilterChain(anyString -> true, Collections.emptyList());
        when(http.build()).thenReturn(dsfc);

        SecurityFilterChain filterChain = config.securityFilterChain(http);
        assertThat(filterChain).isNotNull();
    }

    @Test
    void openApiConfig_Instantiation() {
        OpenApiConfig config = new OpenApiConfig();
        assertThat(config).isNotNull();
    }
}
