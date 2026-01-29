package com.paymentgateway.vault.validator;

import com.paymentgateway.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardValidatorTest {

    // Luhn Algorithm Tests
    @Test
    void testIsValidPan_ValidVisa() {
        assertTrue(CardValidator.isValidPan("4532015112830366")); // Valid Visa test card
        assertTrue(CardValidator.isValidPan("4111111111111111")); // Classic Visa test number
    }

    @Test
    void testIsValidPan_ValidMastercard() {
        assertTrue(CardValidator.isValidPan("5425233430109903")); // Valid Mastercard
        assertTrue(CardValidator.isValidPan("2221000000000009")); // New Mastercard range
    }

    @Test
    void testIsValidPan_ValidAmex() {
        assertTrue(CardValidator.isValidPan("378282246310005")); // Valid Amex
        assertTrue(CardValidator.isValidPan("371449635398431")); // Valid Amex
    }

    @Test
    void testIsValidPan_InvalidLuhn() {
        assertFalse(CardValidator.isValidPan("4532015112830367")); // Invalid checksum
        assertFalse(CardValidator.isValidPan("1234567890123456")); // Random invalid
    }

    @Test
    void testIsValidPan_InvalidLength() {
        assertFalse(CardValidator.isValidPan("123")); // Too short
        assertFalse(CardValidator.isValidPan("12345678901234567890")); // Too long
    }

    @Test
    void testIsValidPan_InvalidFormat() {
        assertFalse(CardValidator.isValidPan("ABC123456789012")); // Contains letters
        assertFalse(CardValidator.isValidPan("")); // Empty
        assertFalse(CardValidator.isValidPan(null)); // Null
    }

    @Test
    void testIsValidPan_WithSpacesAndDashes() {
        assertTrue(CardValidator.isValidPan("4532-0151-1283-0366")); // Valid with dashes
        assertTrue(CardValidator.isValidPan("4532 0151 1283 0366")); // Valid with spaces
    }

    // Brand Detection Tests
    @Test
    void testDetectBrand_Visa() {
        assertEquals(CardValidator.CardBrand.VISA, CardValidator.detectBrand("4532015112830366"));
        assertEquals(CardValidator.CardBrand.VISA, CardValidator.detectBrand("4111111111111111"));
    }

    @Test
    void testDetectBrand_Mastercard() {
        assertEquals(CardValidator.CardBrand.MASTERCARD, CardValidator.detectBrand("5425233430109903"));
        assertEquals(CardValidator.CardBrand.MASTERCARD, CardValidator.detectBrand("2221000000000009"));
    }

    @Test
    void testDetectBrand_Amex() {
        assertEquals(CardValidator.CardBrand.AMEX, CardValidator.detectBrand("378282246310005"));
        assertEquals(CardValidator.CardBrand.AMEX, CardValidator.detectBrand("371449635398431"));
    }

    @Test
    void testDetectBrand_Discover() {
        assertEquals(CardValidator.CardBrand.DISCOVER, CardValidator.detectBrand("6011111111111117"));
        assertEquals(CardValidator.CardBrand.DISCOVER, CardValidator.detectBrand("6445644564456445"));
    }

    @Test
    void testDetectBrand_Unknown() {
        assertEquals(CardValidator.CardBrand.UNKNOWN, CardValidator.detectBrand("9999999999999999"));
        assertEquals(CardValidator.CardBrand.UNKNOWN, CardValidator.detectBrand(""));
        assertEquals(CardValidator.CardBrand.UNKNOWN, CardValidator.detectBrand(null));
    }

    // Expiry Date Tests
    @Test
    void testValidateExpiryDate_Valid() {
        assertDoesNotThrow(() -> CardValidator.validateExpiryDate("12/30"));
        assertDoesNotThrow(() -> CardValidator.validateExpiryDate("12/2030"));
        assertDoesNotThrow(() -> CardValidator.validateExpiryDate("01/35"));
    }

    @Test
    void testValidateExpiryDate_Expired() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> CardValidator.validateExpiryDate("01/25")); // We are in 2026 now
        assertTrue(ex.getMessage().contains("expired"));

        ex = assertThrows(ValidationException.class,
                () -> CardValidator.validateExpiryDate("12/2024"));
        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    void testValidateExpiryDate_InvalidFormat() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> CardValidator.validateExpiryDate("13/25")); // Invalid month
        assertTrue(ex.getMessage().contains("Invalid expiry date format"));

        ex = assertThrows(ValidationException.class,
                () -> CardValidator.validateExpiryDate("2025/12")); // Wrong order
        assertTrue(ex.getMessage().contains("Invalid expiry date format"));

        ex = assertThrows(ValidationException.class,
                () -> CardValidator.validateExpiryDate("invalid"));
        assertTrue(ex.getMessage().contains("Invalid expiry date format"));
    }

    @Test
    void testValidateExpiryDate_Empty() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> CardValidator.validateExpiryDate(""));
        assertTrue(ex.getMessage().contains("required"));

        ex = assertThrows(ValidationException.class,
                () -> CardValidator.validateExpiryDate(null));
        assertTrue(ex.getMessage().contains("required"));
    }

    // Complete Card Validation Tests
    @Test
    void testValidateCard_Valid() {
        assertDoesNotThrow(() -> CardValidator.validateCard("4532015112830366", "12/30"));
        assertDoesNotThrow(() -> CardValidator.validateCard("5425233430109903", "01/2030"));
    }

    @Test
    void testValidateCard_InvalidPan() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> CardValidator.validateCard("1234567890123456", "12/30"));
        assertTrue(ex.getMessage().contains("failed Luhn check"));
    }

    @Test
    void testValidateCard_ExpiredCard() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> CardValidator.validateCard("4532015112830366", "01/25"));
        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    void testValidateCard_UnsupportedBrand() {
        // Create a valid Luhn number that doesn't match known brands
        ValidationException ex = assertThrows(ValidationException.class,
                () -> CardValidator.validateCard("9999999999999995", "12/30"));
        assertTrue(ex.getMessage().contains("Unsupported card brand"));
    }
}
