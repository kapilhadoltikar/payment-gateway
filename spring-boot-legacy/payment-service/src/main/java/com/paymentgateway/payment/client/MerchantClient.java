package com.paymentgateway.payment.client;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.common.dto.merchant.MerchantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class MerchantClient {

    private final RestTemplate restTemplate;

    @Value("${services.merchant.url}")
    private String merchantUrl;

    public MerchantResponse getMerchant(String id) {
        ResponseEntity<ApiResponse<MerchantResponse>> response = restTemplate.exchange(
                merchantUrl + "/merchants/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<MerchantResponse>>() {
                });
        return response.getBody().getData();
    }
}
