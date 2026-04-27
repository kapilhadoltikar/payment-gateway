package com.paymentgateway.common.validation;

import com.paymentgateway.common.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Common validation utilities
 */
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https?://[A-Za-z0-9.-]+(:[0-9]+)?(/.*)?$");

    private ValidationUtils() {
        // Utility class
    }

    /**
     * Validate that a string is not null or empty and return it
     */
    public static String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
        return value;
    }

    /**
     * Validate that a value is not null
     */
    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }
        return value;
    }

    /**
     * Validate email format
     */
    public static void validateEmail(String email) {
        requireNonEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    /**
     * Validate URL format
     */
    public static void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return; // URL is optional in most cases
        }
        if (!URL_PATTERN.matcher(url).matches()) {
            throw new ValidationException("Invalid URL format: " + url);
        }
    }

    /**
     * Validate amount is positive
     */
    public static void validatePositiveAmount(Number amount, String fieldName) {
        requireNonNull(amount, fieldName);
        if (amount.doubleValue() <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
    }

    /**
     * Validate amount is within range
     */
    public static void validateAmountRange(Number amount, double min, double max) {
        requireNonNull(amount, "Amount");
        double value = amount.doubleValue();
        if (value < min || value > max) {
            throw new ValidationException(
                    String.format("Amount must be between %.2f and %.2f", min, max));
        }
    }

    /**
     * Validate string length
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        requireNonEmpty(value, fieldName);
        int length = value.length();
        if (length < minLength || length > maxLength) {
            throw new ValidationException(
                    String.format("%s length must be between %d and %d characters", fieldName, minLength, maxLength));
        }
    }

    /**
     * Validate currency code (ISO 4217)
     */
    public static void validateCurrencyCode(String currency) {
        requireNonEmpty(currency, "Currency");
        if (currency.length() != 3 || !currency.matches("[A-Z]{3}")) {
            throw new ValidationException(
                    "Invalid currency code: " + currency + ". Must be 3-letter ISO code (e.g., USD, EUR)");
        }
    }
}
