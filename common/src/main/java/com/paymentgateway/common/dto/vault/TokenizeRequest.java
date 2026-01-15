package com.paymentgateway.common.dto.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenizeRequest {
    private String pan;
    private String expiryDate;
    private String cardHolderName;
    private String cvv;
}
