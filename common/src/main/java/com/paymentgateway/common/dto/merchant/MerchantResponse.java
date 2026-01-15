package com.paymentgateway.common.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantResponse {
    private String id;
    private String name;
    private String email;
    private String apiKey;
    private String webhookUrl;
    private String status;
    private LocalDateTime createdAt;
}
