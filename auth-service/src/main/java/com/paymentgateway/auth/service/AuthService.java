package com.paymentgateway.auth.service;

import com.paymentgateway.common.exception.BusinessException;
import com.paymentgateway.auth.dto.AuthRequest;
import com.paymentgateway.auth.dto.AuthResponse;
import com.paymentgateway.auth.dto.RegisterRequest;
import com.paymentgateway.auth.entity.User;
import com.paymentgateway.auth.repository.UserRepository;
import com.paymentgateway.common.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists", "USER_EXISTS", 409);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .roles(request.getRoles() == null ? Set.of("MERCHANT") : request.getRoles())
                .build();

        userRepository.save(Objects.requireNonNull(user));

        return login(new AuthRequest(request.getUsername(), request.getPassword()));
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Invalid username or password", "AUTH_FAILED", 401));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid username or password", "AUTH_FAILED", 401);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles());

        String token = jwtUtil.generateToken(user.getUsername(), claims);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .build();
    }
}
