package com.paymentgateway.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CARD, UPI, NET_BANKING, WALLET

    private String cardToken; // If already tokenized

    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String cardHolderName;
    private String cvv;

    @Email(message = "Invalid email format")
    private String customerEmail;

    private String description;
}
