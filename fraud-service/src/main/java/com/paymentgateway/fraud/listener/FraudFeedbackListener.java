package com.paymentgateway.fraud.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.fraud.dto.ChargebackEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudFeedbackListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "chargeback-events", groupId = "fraud-service-group")
    public void onChargebackEvent(String message) {
        try {
            ChargebackEvent event = objectMapper.readValue(message, ChargebackEvent.class);
            log.info("Received Chargeback Event for Transaction: {}. Reasoning: {}", 
                    event.getTransactionId(), event.getReasonCode());
            
            // Logic to persist label for offline training
            // In a real system, this would write to a Data Lake or Feature Store (Offline)
            // For now, we log it as a critical finding
            log.warn("[FEEDBACK LOOP] Transaction {} marked as FRAUD ({})", 
                    event.getTransactionId(), event.getReasonCode());
            
        } catch (Exception e) {
            log.error("Failed to process chargeback event: {}", message, e);
        }
    }
}
