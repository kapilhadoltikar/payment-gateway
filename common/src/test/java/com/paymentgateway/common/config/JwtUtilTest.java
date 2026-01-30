package com.paymentgateway.common.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        JwtUtil util = new JwtUtil();
        this.jwtUtil = util;
        // Inject test values for @Value annotated fields
        ReflectionTestUtils.setField(util, "secret",
                "ThisIsAVeryLongSecretKeyForJWTTokenGenerationAndValidation12345");
        ReflectionTestUtils.setField(util, "expiration", 3600000L);
    }

    @Test
    public void generateToken_CreatesValidToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");

        String token = jwtUtil.generateToken("testuser", claims);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("testuser", jwtUtil.extractUsername(token));

        Claims extractedClaims = jwtUtil.extractAllClaims(token);
        assertEquals("ADMIN", extractedClaims.get("role"));
    }

    @Test
    public void validateToken_WithInvalidToken_ReturnsFalse() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    public void isTokenExpired_WithNewToken_ReturnsFalse() {
        String token = jwtUtil.generateToken("testuser", new HashMap<>());
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @SuppressWarnings("null")
    public void isTokenExpired_WithExpiredToken_ReturnsTrue() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // Set negative expiration
        String token = jwtUtil.generateToken("testuser", new HashMap<>());
        assertTrue(jwtUtil.isTokenExpired(token));
    }
}
