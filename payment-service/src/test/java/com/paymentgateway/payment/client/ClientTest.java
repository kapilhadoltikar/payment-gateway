package com.paymentgateway.payment.client;

import com.paymentgateway.common.dto.ApiResponse;
import com.paymentgateway.common.dto.merchant.MerchantResponse;
import com.paymentgateway.common.dto.vault.CardDataResponse;
import com.paymentgateway.common.dto.vault.TokenizeRequest;
import com.paymentgateway.common.dto.vault.TokenizeResponse;
import com.paymentgateway.fraud.dto.FraudCheckRequest;
import com.paymentgateway.fraud.dto.FraudResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FraudClient fraudClient;

    @InjectMocks
    private MerchantClient merchantClient;

    @InjectMocks
    private VaultClient vaultClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fraudClient, "fraudServiceUrl", "http://fraud");
        ReflectionTestUtils.setField(merchantClient, "merchantUrl", "http://merchant");
        ReflectionTestUtils.setField(vaultClient, "vaultUrl", "http://vault");
    }

    @Test
    void fraudClient_checkFraud_ReturnsResult() {
        FraudCheckRequest request = FraudCheckRequest.builder().transactionId("tx-1").build();
        FraudResult result = FraudResult.builder().decision(FraudResult.FraudDecision.APPROVE).build();
        ApiResponse<FraudResult> apiResponse = ApiResponse.success(result);

        when(restTemplate.exchange(eq("http://fraud/api/v1/fraud/check"), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        FraudResult actual = fraudClient.checkFraud(request);
        assertThat(actual.getDecision()).isEqualTo(FraudResult.FraudDecision.APPROVE);
    }

    @Test
    void fraudClient_checkFraud_HandlesException() {
        FraudCheckRequest request = FraudCheckRequest.builder().transactionId("tx-1").build();
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("API Down"));

        FraudResult actual = fraudClient.checkFraud(request);
        assertThat(actual.getDecision()).isEqualTo(FraudResult.FraudDecision.MANUAL_REVIEW);
    }

    @Test
    void merchantClient_getMerchant_ReturnsResult() {
        MerchantResponse result = MerchantResponse.builder().id("m-1").build();
        ApiResponse<MerchantResponse> apiResponse = ApiResponse.success(result);

        when(restTemplate.exchange(eq("http://merchant/merchants/m-1"), eq(HttpMethod.GET), any(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        MerchantResponse actual = merchantClient.getMerchant("m-1");
        assertThat(actual.getId()).isEqualTo("m-1");
    }

    @Test
    void vaultClient_tokenize_ReturnsResult() {
        TokenizeRequest request = TokenizeRequest.builder().pan("1234").build();
        TokenizeResponse result = TokenizeResponse.builder().token("tok-1").build();
        ApiResponse<TokenizeResponse> apiResponse = ApiResponse.success(result);

        when(restTemplate.exchange(eq("http://vault/vault/tokenize"), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        TokenizeResponse actual = vaultClient.tokenize(request);
        assertThat(actual.getToken()).isEqualTo("tok-1");
    }

    @Test
    void vaultClient_detokenize_ReturnsResult() {
        CardDataResponse result = CardDataResponse.builder().pan("1234").build();
        ApiResponse<CardDataResponse> apiResponse = ApiResponse.success(result);

        when(restTemplate.exchange(eq("http://vault/vault/detokenize/tok-1"), eq(HttpMethod.GET), any(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        CardDataResponse actual = vaultClient.detokenize("tok-1");
        assertThat(actual.getPan()).isEqualTo("1234");
    }
}
