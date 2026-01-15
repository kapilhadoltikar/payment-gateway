package com.paymentgateway.notification.messaging;

import com.paymentgateway.common.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPaymentConsumer {

    private final RabbitMQProducer rabbitMQProducer;

    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void consume(Transaction transaction) {
        log.info("Consumed payment event from Kafka: {}", transaction.getId());

        // Push to RabbitMQ for webhook delivery
        rabbitMQProducer.sendWebhookTask(transaction);
    }
}
