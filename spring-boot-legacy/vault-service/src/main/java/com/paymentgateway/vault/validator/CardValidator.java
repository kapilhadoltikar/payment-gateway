package com.paymentgateway.vault.validator;

import com.paymentgateway.common.exception.ValidationException;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Card validation utilities including Luhn algorithm and brand detection
 */
public class CardValidator {

    private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter.ofPattern("MM/[uuuu][uu]");

    private CardValidator() {
        // Utility class
    }

    /**
     * Validate card number using Luhn algorithm (mod 10)
     */
    public static boolean isValidPan(String pan) {
        if (pan == null || pan.isEmpty()) {
            return false;
        }

        // Remove spaces and dashes
        String cleanPan = pan.replaceAll("[\\s-]", "");

        // Check if all digits
        if (!cleanPan.matches("\\d+")) {
            return false;
        }

        // Check length (13-19 digits)
        if (cleanPan.length() < 13 || cleanPan.length() > 19) {
            return false;
        }

        // Luhn algorithm
        int sum = 0;
        boolean alternate = false;
        for (int i = cleanPan.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleanPan.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    /**
     * Detect card brand from PAN
     */
    public static CardBrand detectBrand(String pan) {
        if (pan == null || pan.isEmpty()) {
            return CardBrand.UNKNOWN;
        }

        String cleanPan = pan.replaceAll("[\\s-]", "");

        // Visa: starts with 4
        if (cleanPan.startsWith("4")) {
            return CardBrand.VISA;
        }

        // Mastercard: 51-55 or 2221-2720
        if (cleanPan.matches("^5[1-5].*") ||
                (cleanPan.length() >= 4 &&
                        Integer.parseInt(cleanPan.substring(0, 4)) >= 2221 &&
                        Integer.parseInt(cleanPan.substring(0, 4)) <= 2720)) {
            return CardBrand.MASTERCARD;
        }

        // American Express: 34 or 37
        if (cleanPan.matches("^3[47].*")) {
            return CardBrand.AMEX;
        }

        // Discover: 6011, 622126-622925, 644-649, 65
        if (cleanPan.startsWith("6011") ||
                cleanPan.matches("^64[4-9].*") ||
                cleanPan.startsWith("65")) {
            return CardBrand.DISCOVER;
        }

        return CardBrand.UNKNOWN;
    }

    /**
     * Validate expiry date format and check if card is expired
     */
    public static void validateExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            throw new ValidationException("Expiry date is required");
        }

        try {
            YearMonth expiry = YearMonth.parse(expiryDate, EXPIRY_FORMATTER);
            YearMonth now = YearMonth.now();

            if (expiry.isBefore(now)) {
                throw new ValidationException("Card has expired");
            }
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid expiry date format. Expected MM/YY or MM/YYYY");
        }
    }

    /**
     * Validate complete card data
     */
    public static void validateCard(String pan, String expiryDate) {
        if (!isValidPan(pan)) {
            throw new ValidationException("Invalid card number (failed Luhn check)");
        }

        validateExpiryDate(expiryDate);

        CardBrand brand = detectBrand(pan);
        if (brand == CardBrand.UNKNOWN) {
            throw new ValidationException("Unsupported card brand");
        }
    }

    /**
     * Card brand enumeration
     */
    public enum CardBrand {
        VISA,
        MASTERCARD,
        AMEX,
        DISCOVER,
        UNKNOWN
    }
}
