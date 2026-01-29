package com.paymentgateway.common.validation;

import com.paymentgateway.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void testRequireNonEmpty_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("test", "field"));
    }

    @Test
    void testRequireNonEmpty_Null() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> ValidationUtils.requireNonEmpty(null, "field"));
        assertTrue(ex.getMessage().contains("cannot be empty"));
    }

    @Test
    void testRequireNonEmpty_Empty() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> ValidationUtils.requireNonEmpty("", "field"));
        assertTrue(ex.getMessage().contains("cannot be empty"));
    }

    @Test
    void testRequireNonEmpty_Whitespace() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> ValidationUtils.requireNonEmpty("   ", "field"));
        assertTrue(ex.getMessage().contains("cannot be empty"));
    }

    @Test
    void testRequireNonNull_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonNull("test", "field"));
    }

    @Test
    void testRequireNonNull_Null() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> ValidationUtils.requireNonNull(null, "field"));
        assertTrue(ex.getMessage().contains("cannot be null"));
    }

    @Test
    void testValidateEmail_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.validateEmail("test@example.com"));
        assertDoesNotThrow(() -> ValidationUtils.validateEmail("user+tag@domain.co.uk"));
    }

    @Test
    void testValidateEmail_Invalid() {
        assertThrows(ValidationException.class, () -> ValidationUtils.validateEmail("invalid"));
        assertThrows(ValidationException.class, () -> ValidationUtils.validateEmail("@example.com"));
        assertThrows(ValidationException.class, () -> ValidationUtils.validateEmail("test@"));
        assertThrows(ValidationException.class, () -> ValidationUtils.validateEmail("test @example.com"));
    }

    @Test
    void testValidateUrl_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.validateUrl("https://example.com"));
        assertDoesNotThrow(() -> ValidationUtils.validateUrl("http://localhost:8080/webhook"));
        assertDoesNotThrow(() -> ValidationUtils.validateUrl(null)); // Optional
        assertDoesNotThrow(() -> ValidationUtils.validateUrl("")); // Optional
    }

    @Test
    void testValidateUrl_Invalid() {
        assertThrows(ValidationException.class, () -> ValidationUtils.validateUrl("not-a-url"));
        assertThrows(ValidationException.class, () -> ValidationUtils.validateUrl("ftp://example.com"));
    }

    @Test
    void testValidatePositiveAmount_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.validatePositiveAmount(100.0, "amount"));
        assertDoesNotThrow(() -> ValidationUtils.validatePositiveAmount(0.01, "amount"));
    }

    @Test
    void testValidatePositiveAmount_Invalid() {
        assertThrows(ValidationException.class,
                () -> ValidationUtils.validatePositiveAmount(0, "amount"));
        assertThrows(ValidationException.class,
                () -> ValidationUtils.validatePositiveAmount(-10.0, "amount"));
        assertThrows(ValidationException.class,
                () -> ValidationUtils.validatePositiveAmount(null, "amount"));
    }

    @Test
    void testValidateAmountRange_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.validateAmountRange(50.0, 10.0, 100.0));
        assertDoesNotThrow(() -> ValidationUtils.validateAmountRange(10.0, 10.0, 100.0)); // Min boundary
        assertDoesNotThrow(() -> ValidationUtils.validateAmountRange(100.0, 10.0, 100.0)); // Max boundary
    }

    @Test
    void testValidateAmountRange_Invalid() {
        assertThrows(ValidationException.class,
                () -> ValidationUtils.validateAmountRange(5.0, 10.0, 100.0));
        assertThrows(ValidationException.class,
                () -> ValidationUtils.validateAmountRange(150.0, 10.0, 100.0));
    }

    @Test
    void testValidateLength_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.validateLength("test", "field", 1, 10));
        assertDoesNotThrow(() -> ValidationUtils.validateLength("a", "field", 1, 10)); // Min boundary
        assertDoesNotThrow(() -> ValidationUtils.validateLength("1234567890", "field", 1, 10)); // Max boundary
    }

    @Test
    void testValidateLength_Invalid() {
        assertThrows(ValidationException.class,
                () -> ValidationUtils.validateLength("", "field", 1, 10));
        assertThrows(ValidationException.class,
                () -> ValidationUtils.validateLength("12345678901", "field", 1, 10));
    }

    @Test
    void testValidateCurrencyCode_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.validateCurrencyCode("USD"));
        assertDoesNotThrow(() -> ValidationUtils.validateCurrencyCode("EUR"));
        assertDoesNotThrow(() -> ValidationUtils.validateCurrencyCode("GBP"));
    }

    @Test
    void testValidateCurrencyCode_Invalid() {
        assertThrows(ValidationException.class, () -> ValidationUtils.validateCurrencyCode("US"));
        assertThrows(ValidationException.class, () -> ValidationUtils.validateCurrencyCode("USDT"));
        assertThrows(ValidationException.class, () -> ValidationUtils.validateCurrencyCode("usd"));
        assertThrows(ValidationException.class, () -> ValidationUtils.validateCurrencyCode("123"));
    }
}
