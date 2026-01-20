package com.paymentgateway.merchant.service;

import com.paymentgateway.common.dto.merchant.MerchantRequest;
import com.paymentgateway.common.dto.merchant.MerchantResponse;
import com.paymentgateway.common.exception.BusinessException;
import com.paymentgateway.common.validation.ValidationUtils;
import com.paymentgateway.merchant.entity.Merchant;
import com.paymentgateway.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final ApiKeyService apiKeyService;

    public MerchantResponse createMerchant(MerchantRequest request) {
        // Validate input
        ValidationUtils.requireNonEmpty(request.getName(), "Merchant name");
        ValidationUtils.validateEmail(request.getEmail());
        ValidationUtils.validateUrl(request.getWebhookUrl());

        if (merchantRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Merchant with email already exists", "MERCHANT_DUPLICATE_EMAIL", 409);
        }

        // Generate and hash API key securely
        String plainApiKey = apiKeyService.generateApiKey(false); // Live key
        String hashedApiKey = apiKeyService.hashApiKey(plainApiKey);

        Merchant merchant = Merchant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .apiKey(hashedApiKey) // Store hashed key
                .webhookUrl(request.getWebhookUrl())
                .status(Merchant.MerchantStatus.ACTIVE)
                .build();

        Merchant saved = merchantRepository.save(merchant);

        // Return response with plain API key (show only once!)
        MerchantResponse response = mapToResponse(saved);
        response.setApiKey(plainApiKey); // Return plain key only on creation
        return response;
    }

    public MerchantResponse getMerchant(String id) {
        ValidationUtils.requireNonEmpty(id, "Merchant ID");

        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Merchant not found", "MERCHANT_NOT_FOUND", 404));

        MerchantResponse response = mapToResponse(merchant);
        response.setApiKey("pk_****"); // Mask API key in responses
        return response;
    }

    /**
     * Validate API key - for authentication purposes
     */
    public boolean validateMerchantApiKey(String merchantId, String providedApiKey) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException("Merchant not found", "MERCHANT_NOT_FOUND", 404));

        if (merchant.getStatus() != Merchant.MerchantStatus.ACTIVE) {
            throw new BusinessException("Merchant account is not active", "MERCHANT_INACTIVE", 403);
        }

        return apiKeyService.validateApiKey(providedApiKey, merchant.getApiKey());
    }

    private MerchantResponse mapToResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .apiKey(merchant.getApiKey()) // Will be masked or replaced by caller
                .webhookUrl(merchant.getWebhookUrl())
                .status(merchant.getStatus().name())
                .createdAt(merchant.getCreatedAt())
                .build();
    }
}
