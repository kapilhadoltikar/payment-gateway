package com.paymentgateway.payment.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "payment-events";

    public void sendPaymentEvent(Object event) {
        log.info("Sending payment event to Kafka: {}", event);
        kafkaTemplate.send(TOPIC, event);
    }
}
