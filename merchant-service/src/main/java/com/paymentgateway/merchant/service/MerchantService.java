package com.paymentgateway.merchant.service;

import com.paymentgateway.common.dto.merchant.MerchantRequest;
import com.paymentgateway.common.dto.merchant.MerchantResponse;
import com.paymentgateway.merchant.entity.Merchant;
import com.paymentgateway.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantResponse createMerchant(MerchantRequest request) {
        if (merchantRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Merchant with email already exists");
        }

        // In production, use a secure API key generation and hashing
        String apiKey = "pk_" + UUID.randomUUID().toString().replace("-", "");

        Merchant merchant = Merchant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .apiKey(apiKey) // In production, hash this
                .webhookUrl(request.getWebhookUrl())
                .status(Merchant.MerchantStatus.ACTIVE)
                .build();

        Merchant saved = merchantRepository.save(merchant);

        return mapToResponse(saved);
    }

    public MerchantResponse getMerchant(String id) {
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));
        return mapToResponse(merchant);
    }

    private MerchantResponse mapToResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .apiKey(merchant.getApiKey())
                .webhookUrl(merchant.getWebhookUrl())
                .status(merchant.getStatus().name())
                .createdAt(merchant.getCreatedAt())
                .build();
    }
}
