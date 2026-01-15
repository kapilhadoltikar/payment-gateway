package com.paymentgateway.common.dto.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDataResponse {
    private String pan;
    private String expiryDate;
    private String cardHolderName;
}
