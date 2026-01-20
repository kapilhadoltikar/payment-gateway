package com.paymentgateway.auth.controller;

import com.paymentgateway.auth.dto.AuthRequest;
import com.paymentgateway.auth.dto.AuthResponse;
import com.paymentgateway.auth.dto.RegisterRequest;
import com.paymentgateway.auth.service.AuthService;
import com.paymentgateway.common.config.JwtUtil;
import com.paymentgateway.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            boolean valid = jwtUtil.validateToken(token);
            return ResponseEntity.ok(ApiResponse.success(valid));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success(false));
        }
    }
}
