package com.paymentgateway.merchant.service;

import com.paymentgateway.common.exception.BusinessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Secure API key generation and validation service
 */
@Service
public class ApiKeyService {

    private static final String API_KEY_PREFIX = "pk_live_";
    private static final String TEST_KEY_PREFIX = "pk_test_";
    private static final int KEY_LENGTH = 32;

    private final BCryptPasswordEncoder encoder;

    public ApiKeyService() {
        this.encoder = new BCryptPasswordEncoder(12); // Strength 12 for API keys
    }

    /**
     * Generate a secure API key
     * 
     * @param isTestKey whether this is a test key or live key
     * @return the plain text API key (show only once to merchant)
     */
    public String generateApiKey(boolean isTestKey) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[KEY_LENGTH];
        random.nextBytes(bytes);

        String randomPart = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        String prefix = isTestKey ? TEST_KEY_PREFIX : API_KEY_PREFIX;
        return prefix + randomPart;
    }

    /**
     * Hash API key for secure storage
     */
    public String hashApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new BusinessException("API key cannot be empty");
        }
        return encoder.encode(apiKey);
    }

    /**
     * Validate that a given API key matches the hash
     */
    public boolean validateApiKey(String plainKey, String hashedKey) {
        if (plainKey == null || hashedKey == null) {
            return false;
        }
        return encoder.matches(plainKey, hashedKey);
    }

    /**
     * Check if API key has the correct format
     */
    public boolean isValidFormat(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }

        return apiKey.startsWith(API_KEY_PREFIX) || apiKey.startsWith(TEST_KEY_PREFIX);
    }

    /**
     * Check if API key is a test key
     */
    public boolean isTestKey(String apiKey) {
        if (apiKey == null) {
            return false;
        }
        return apiKey.startsWith(TEST_KEY_PREFIX);
    }
}
