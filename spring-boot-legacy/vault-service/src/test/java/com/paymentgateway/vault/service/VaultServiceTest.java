package com.paymentgateway.vault.service;

import com.paymentgateway.common.dto.vault.CardDataResponse;
import com.paymentgateway.common.dto.vault.TokenizeRequest;
import com.paymentgateway.common.dto.vault.TokenizeResponse;
import com.paymentgateway.common.exception.BusinessException;
import com.paymentgateway.common.security.EncryptionService;
import com.paymentgateway.vault.entity.CardData;
import com.paymentgateway.vault.repository.CardDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VaultServiceTest {

        @Mock
        private CardDataRepository cardDataRepository;

        @Mock
        private EncryptionService encryptionService;

        @InjectMocks
        private VaultService vaultService;

        @Test
        public void tokenize_Success() {
                TokenizeRequest request = TokenizeRequest.builder()
                                .pan("4242424242424242")
                                .expiryDate("12/30")
                                .cvv("123")
                                .cardHolderName("John Doe")
                                .build();

                when(encryptionService.encrypt("4242424242424242")).thenReturn("encrypted_pan");

                CardData saved = CardData.builder()
                                .id("token_123")
                                .encryptedPan("encrypted_pan")
                                .build();

                when(cardDataRepository.save(any(CardData.class))).thenReturn(saved);

                TokenizeResponse result = vaultService.tokenize(request);

                assertNotNull(result);
                assertEquals("token_123", result.getToken());
                assertEquals("4242", result.getLastFour());
        }

        @Test
        public void detokenize_Success() {
                CardData cardData = CardData.builder()
                                .id("token_123")
                                .encryptedPan("encrypted_pan")
                                .expiryDate("12/26")
                                .cardHolderName("John Doe")
                                .build();

                when(cardDataRepository.findById("token_123")).thenReturn(Optional.of(cardData));
                when(encryptionService.decrypt("encrypted_pan")).thenReturn("4111222233334444");

                CardDataResponse response = vaultService.detokenize("token_123");

                assertNotNull(response);
                assertEquals("4111222233334444", response.getPan());
        }

        @Test
        public void detokenize_NotFound_ThrowsException() {
                when(cardDataRepository.findById("not_found")).thenReturn(Optional.empty());
                assertThrows(BusinessException.class, () -> vaultService.detokenize("not_found"));
        }
}
