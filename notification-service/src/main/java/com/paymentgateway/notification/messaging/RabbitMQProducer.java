package com.paymentgateway.notification.messaging;

import com.paymentgateway.common.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "webhook-exchange";
    private static final String ROUTING_KEY = "webhook-routing-key";

    public void sendWebhookTask(Transaction transaction) {
        log.info("Sending webhook task to RabbitMQ for transaction: {}", transaction.getId());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, transaction);
    }
}
