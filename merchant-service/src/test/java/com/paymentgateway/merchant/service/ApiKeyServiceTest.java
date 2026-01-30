package com.paymentgateway.merchant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApiKeyServiceTest {

    private ApiKeyService apiKeyService;

    @BeforeEach
    public void setUp() {
        apiKeyService = new ApiKeyService();
    }

    @Test
    public void generateApiKey_LiveKey_HasCorrectPrefix() {
        String key = apiKeyService.generateApiKey(false);
        assertTrue(key.startsWith("pk_live_"));
        assertTrue(key.length() > 40);
    }

    @Test
    public void generateApiKey_TestKey_HasCorrectPrefix() {
        String key = apiKeyService.generateApiKey(true);
        assertTrue(key.startsWith("pk_test_"));
    }

    @Test
    public void hashAndValidate_MatchesCorrectly() {
        String key = "pk_live_some-random-key";
        String hashed = apiKeyService.hashApiKey(key);

        assertTrue(apiKeyService.validateApiKey(key, hashed));
        assertFalse(apiKeyService.validateApiKey("wrong-key", hashed));
    }

    @Test
    public void hashApiKey_Invalid_ThrowsException() {
        assertThrows(com.paymentgateway.common.exception.BusinessException.class, () -> apiKeyService.hashApiKey(""));
        assertThrows(com.paymentgateway.common.exception.BusinessException.class, () -> apiKeyService.hashApiKey(null));
    }

    @Test
    public void validateApiKey_Nulls_ReturnsFalse() {
        assertFalse(apiKeyService.validateApiKey(null, "hashed"));
        assertFalse(apiKeyService.validateApiKey("plain", null));
    }

    @Test
    public void isValidFormat_CorrectlyIdentifiesPrefixes() {
        assertTrue(apiKeyService.isValidFormat("pk_live_abc"));
        assertTrue(apiKeyService.isValidFormat("pk_test_abc"));
        assertFalse(apiKeyService.isValidFormat("invalid_abc"));
        assertFalse(apiKeyService.isValidFormat(null));
        assertFalse(apiKeyService.isValidFormat(""));
    }

    @Test
    public void isTestKey_CorrectlyIdentifiesTestKeys() {
        assertTrue(apiKeyService.isTestKey("pk_test_abc"));
        assertFalse(apiKeyService.isTestKey("pk_live_abc"));
        assertFalse(apiKeyService.isTestKey(null));
    }
}
