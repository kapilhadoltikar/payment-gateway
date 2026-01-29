package com.paymentgateway.merchant.service;

import com.paymentgateway.common.dto.merchant.MerchantRequest;
import com.paymentgateway.common.dto.merchant.MerchantResponse;
import com.paymentgateway.common.exception.BusinessException;
import com.paymentgateway.merchant.entity.Merchant;
import com.paymentgateway.merchant.repository.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MerchantServiceTest {

        @Mock
        private MerchantRepository merchantRepository;

        @Mock
        private ApiKeyService apiKeyService;

        @InjectMocks
        private MerchantService merchantService;

        @Test
        public void createMerchant_Success() {
                MerchantRequest request = MerchantRequest.builder()
                                .name("Test Merchant")
                                .email("test@example.com")
                                .webhookUrl("https://example.com/webhook")
                                .build();

                when(merchantRepository.findByEmail(anyString())).thenReturn(Optional.empty());
                when(apiKeyService.generateApiKey(false)).thenReturn("pk_live_plain");
                when(apiKeyService.hashApiKey("pk_live_plain")).thenReturn("hashed_key");

                Merchant savedMerchant = Merchant.builder()
                                .id(UUID.randomUUID().toString())
                                .name("Test Merchant")
                                .email("test@example.com")
                                .apiKey("hashed_key")
                                .status(Merchant.MerchantStatus.ACTIVE)
                                .build();

                when(merchantRepository.save(any(Merchant.class))).thenReturn(savedMerchant);

                MerchantResponse response = merchantService.createMerchant(request);

                assertNotNull(response);
                assertEquals("pk_live_plain", response.getApiKey());
                assertEquals("Test Merchant", response.getName());
                verify(merchantRepository).save(any(Merchant.class));
        }

        @Test
        public void createMerchant_DuplicateEmail_ThrowsException() {
                MerchantRequest request = MerchantRequest.builder()
                                .name("Test Merchant")
                                .email("duplicate@example.com")
                                .webhookUrl("https://example.com/webhook")
                                .build();

                when(merchantRepository.findByEmail("duplicate@example.com")).thenReturn(Optional.of(new Merchant()));

                assertThrows(BusinessException.class, () -> merchantService.createMerchant(request));
        }

        @Test
        public void getMerchant_NotFound_ThrowsException() {
                when(merchantRepository.findById("unknown")).thenReturn(Optional.empty());
                assertThrows(BusinessException.class, () -> merchantService.getMerchant("unknown"));
        }

        @Test
        public void validateMerchantApiKey_Success() {
                Merchant merchant = Merchant.builder()
                                .id("m1")
                                .apiKey("hashed")
                                .status(Merchant.MerchantStatus.ACTIVE)
                                .build();

                when(merchantRepository.findById("m1")).thenReturn(Optional.of(merchant));
                when(apiKeyService.validateApiKey("plain", "hashed")).thenReturn(true);

                assertTrue(merchantService.validateMerchantApiKey("m1", "plain"));
        }
}
