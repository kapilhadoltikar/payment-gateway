package com.paymentgateway.fraud.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.fraud.dto.ChargebackEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudFeedbackListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FraudFeedbackListener listener;

    @Test
    void onChargebackEvent_ValidMessage_Success() throws Exception {
        String message = "{\"transactionId\":\"tx123\", \"reasonCode\":\"FRAUD\"}";
        ChargebackEvent event = new ChargebackEvent();
        event.setTransactionId("tx123");
        event.setReasonCode("FRAUD");

        when(objectMapper.readValue(message, ChargebackEvent.class)).thenReturn(event);

        listener.onChargebackEvent(message);

        verify(objectMapper).readValue(message, ChargebackEvent.class);
    }

    @Test
    void onChargebackEvent_InvalidMessage_HandlesException() throws Exception {
        String message = "invalid json";
        when(objectMapper.readValue(anyString(), eq(ChargebackEvent.class)))
                .thenThrow(new RuntimeException("JSON Parse Error"));

        listener.onChargebackEvent(message);

        verify(objectMapper).readValue(message, ChargebackEvent.class);
    }
}
