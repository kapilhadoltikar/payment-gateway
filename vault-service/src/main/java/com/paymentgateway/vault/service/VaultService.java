package com.paymentgateway.vault.service;

import com.paymentgateway.common.security.EncryptionService;
import com.paymentgateway.common.dto.vault.CardDataResponse;
import com.paymentgateway.common.dto.vault.TokenizeRequest;
import com.paymentgateway.common.dto.vault.TokenizeResponse;
import com.paymentgateway.vault.entity.CardData;
import com.paymentgateway.vault.repository.CardDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VaultService {

        private final CardDataRepository cardDataRepository;
        private final EncryptionService encryptionService;

        public TokenizeResponse tokenize(TokenizeRequest request) {
                String encryptedPan = encryptionService.encrypt(request.getPan());

                CardData cardData = CardData.builder()
                                .encryptedPan(encryptedPan)
                                .expiryDate(request.getExpiryDate())
                                .cardHolderName(request.getCardHolderName())
                                .build();

                CardData saved = cardDataRepository.save(cardData);

                return TokenizeResponse.builder()
                                .token(saved.getId())
                                .lastFour(request.getPan().substring(request.getPan().length() - 4))
                                .build();
        }

        public CardDataResponse detokenize(String token) {
                CardData cardData = cardDataRepository.findById(token)
                                .orElseThrow(() -> new RuntimeException("Token not found"));

                String pan = encryptionService.decrypt(cardData.getEncryptedPan());

                return CardDataResponse.builder()
                                .pan(pan)
                                .expiryDate(cardData.getExpiryDate())
                                .cardHolderName(cardData.getCardHolderName())
                                .build();
        }
}
