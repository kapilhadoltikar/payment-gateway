package com.paymentgateway.auth.service;

import com.paymentgateway.auth.dto.AuthRequest;
import com.paymentgateway.auth.dto.AuthResponse;
import com.paymentgateway.auth.dto.RegisterRequest;
import com.paymentgateway.auth.entity.User;
import com.paymentgateway.auth.repository.UserRepository;
import com.paymentgateway.common.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest("user", "pass", "user@test.com", Set.of("USER"));
        User savedUser = User.builder().username("user").password("encoded").email("user@test.com")
                .roles(Set.of("USER")).build();

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(eq("user"), any())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateUsername() {
        RegisterRequest request = new RegisterRequest("user", "pass", "user@test.com", null);
        when(userRepository.existsByUsername("user")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test
    void login_Success() {
        AuthRequest request = new AuthRequest("user", "pass");
        User user = User.builder().username("user").password("encoded").email("user@test.com").roles(Set.of("USER"))
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(eq("user"), any())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void login_InvalidPassword() {
        AuthRequest request = new AuthRequest("user", "wrong");
        User user = User.builder().username("user").password("encoded").build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }
}
