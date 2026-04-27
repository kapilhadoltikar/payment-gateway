package com.paymentgateway.merchant.controller;

import com.paymentgateway.common.dto.merchant.MerchantRequest;
import com.paymentgateway.common.dto.merchant.MerchantResponse;
import com.paymentgateway.merchant.service.MerchantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class MerchantControllerTest {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        @MockitoBean
        private MerchantService merchantService;

        private ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }

        @Test
        public void createMerchant_ReturnsSuccess() throws Exception {
                MerchantRequest request = MerchantRequest.builder()
                                .name("Test Merchant")
                                .email("test@example.com")
                                .webhookUrl("https://example.com/webhook")
                                .build();

                MerchantResponse response = MerchantResponse.builder()
                                .id("m1")
                                .name("Test Merchant")
                                .build();

                when(merchantService.createMerchant(any(MerchantRequest.class))).thenReturn(response);

                mockMvc.perform(post("/merchants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value("m1"));
        }

        @Test
        public void getMerchant_ReturnsMerchant() throws Exception {
                MerchantResponse response = MerchantResponse.builder()
                                .id("m1")
                                .name("Test Merchant")
                                .build();

                when(merchantService.getMerchant("m1")).thenReturn(response);

                mockMvc.perform(get("/merchants/m1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.name").value("Test Merchant"));
        }
}
