package com.paymentgateway.common.dto;

import com.paymentgateway.common.dto.merchant.MerchantRequest;
import com.paymentgateway.common.dto.merchant.MerchantResponse;
import com.paymentgateway.common.dto.vault.CardDataResponse;
import com.paymentgateway.common.dto.vault.TokenizeRequest;
import com.paymentgateway.common.dto.vault.TokenizeResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class DtoTest {

    @Test
    void merchantDtos_WorkCorrectly() {
        MerchantRequest request = MerchantRequest.builder()
                .name("Merchant 1")
                .email("m1@example.com")
                .webhookUrl("http://hook.com")
                .build();
        assertThat(request.getName()).isEqualTo("Merchant 1");
        assertThat(request.getEmail()).isEqualTo("m1@example.com");
        assertThat(request.getWebhookUrl()).isEqualTo("http://hook.com");

        MerchantResponse response = MerchantResponse.builder()
                .id("1")
                .name("Merchant 1")
                .status("ACTIVE")
                .apiKey("api-123")
                .createdAt(LocalDateTime.now())
                .build();
        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getApiKey()).isEqualTo("api-123");
    }

    @Test
    void vaultDtos_WorkCorrectly() {
        TokenizeRequest req = TokenizeRequest.builder()
                .pan("1234567812345678")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .cvv("123")
                .build();
        assertThat(req.getPan()).isEqualTo("1234567812345678");
        assertThat(req.getExpiryDate()).isEqualTo("12/25");

        TokenizeResponse res = TokenizeResponse.builder()
                .token("tok-123")
                .lastFour("5678")
                .build();
        assertThat(res.getToken()).isEqualTo("tok-123");
        assertThat(res.getLastFour()).isEqualTo("5678");

        CardDataResponse cardRes = CardDataResponse.builder()
                .pan("1234567812345678")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .build();
        assertThat(cardRes.getPan()).isEqualTo("1234567812345678");
        assertThat(cardRes.getExpiryDate()).isEqualTo("12/25");
    }
}
