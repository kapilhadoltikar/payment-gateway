package com.paymentgateway.payment.client;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.common.dto.vault.CardDataResponse;
import com.paymentgateway.common.dto.vault.TokenizeRequest;
import com.paymentgateway.common.dto.vault.TokenizeResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class VaultClient {

    private final RestTemplate restTemplate;

    @Value("${services.vault.url}")
    private String vaultUrl;

    public TokenizeResponse tokenize(TokenizeRequest request) {
        String url = vaultUrl + "/vault/tokenize";
        System.out.println("DEBUG: VaultClient calling URL: " + url);
        ResponseEntity<ApiResponse<TokenizeResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<ApiResponse<TokenizeResponse>>() {
                });
        return response.getBody().getData();
    }

    public CardDataResponse detokenize(String token) {
        ResponseEntity<ApiResponse<CardDataResponse>> response = restTemplate.exchange(
                vaultUrl + "/vault/detokenize/" + token,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<CardDataResponse>>() {
                });
        return response.getBody().getData();
    }
}
