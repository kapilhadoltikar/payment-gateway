package com.paymentgateway.merchant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.common.dto.merchant.MerchantRequest;
import com.paymentgateway.merchant.service.MerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@WebMvcTest(MerchantController.class)
public class MerchantControllerDocsTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private MerchantService merchantService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        public void setUp(WebApplicationContext webApplicationContext,
                        RestDocumentationContextProvider restDocumentation) {
                this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(documentationConfiguration(restDocumentation))
                                .build();
        }

        @Test
        public void createMerchant() throws Exception {
                MerchantRequest request = new MerchantRequest();
                request.setName("Test Merchant");
                request.setEmail("test@merchant.com");
                request.setWebhookUrl("http://localhost:8080/callback");

                this.mockMvc.perform(post("/merchants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(this.objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andDo(document("create-merchant",
                                                requestFields(
                                                                fieldWithPath("name").description(
                                                                                "The name of the merchant"),
                                                                fieldWithPath("email").description(
                                                                                "The email of the merchant"),
                                                                fieldWithPath("webhookUrl").description(
                                                                                "The webhook URL for payment notifications")),
                                                responseFields(
                                                                fieldWithPath("success").description(
                                                                                "Whether the operation was successful"),
                                                                fieldWithPath("message").description(
                                                                                "A message describing the result"),
                                                                fieldWithPath("data")
                                                                                .description("The merchant details")
                                                                                .optional(),
                                                                fieldWithPath("errorCode")
                                                                                .description("The error code if failed")
                                                                                .optional(),
                                                                fieldWithPath("timestamp").description(
                                                                                "The response timestamp"))));
        }

        @Test
        public void getMerchant() throws Exception {
                this.mockMvc.perform(get("/merchants/{id}", "MERCH-123")
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andDo(document("get-merchant",
                                                responseFields(
                                                                fieldWithPath("success").description(
                                                                                "Whether the operation was successful"),
                                                                fieldWithPath("message").description(
                                                                                "A message describing the result")
                                                                                .optional(),
                                                                fieldWithPath("data")
                                                                                .description("The merchant details")
                                                                                .optional(),
                                                                fieldWithPath("errorCode")
                                                                                .description("The error code if failed")
                                                                                .optional(),
                                                                fieldWithPath("timestamp").description(
                                                                                "The response timestamp"))));
        }
}
