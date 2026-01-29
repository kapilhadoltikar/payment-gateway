package com.paymentgateway.vault.controller;

import com.paymentgateway.common.dto.vault.CardDataResponse;
import com.paymentgateway.common.dto.vault.TokenizeRequest;
import com.paymentgateway.common.dto.vault.TokenizeResponse;
import com.paymentgateway.vault.service.VaultService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VaultController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VaultControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private VaultService vaultService;

        private ObjectMapper objectMapper;

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
        }

        @Test
        public void tokenize_ReturnsSuccess() throws Exception {
                TokenizeRequest request = TokenizeRequest.builder()
                                .pan("4111222233334444")
                                .expiryDate("12/26")
                                .cardHolderName("John Doe")
                                .build();

                TokenizeResponse response = TokenizeResponse.builder()
                                .token("token_123")
                                .lastFour("4444")
                                .build();

                when(vaultService.tokenize(any(TokenizeRequest.class))).thenReturn(response);

                mockMvc.perform(post("/vault/tokenize")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.token").value("token_123"));
        }

        @Test
        public void detokenize_ReturnsCardData() throws Exception {
                CardDataResponse response = CardDataResponse.builder()
                                .pan("4111222233334444")
                                .expiryDate("12/26")
                                .build();

                when(vaultService.detokenize("token_123")).thenReturn(response);

                mockMvc.perform(get("/vault/detokenize/token_123"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.pan").value("4111222233334444"));
        }
}
