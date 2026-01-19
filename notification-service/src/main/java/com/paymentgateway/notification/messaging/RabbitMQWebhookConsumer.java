package com.paymentgateway.notification.messaging;

import com.paymentgateway.common.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQWebhookConsumer {

    @RabbitListener(queues = "webhook-queue")
    public void consume(Transaction transaction) {
        log.info("Processing webhook delivery for transaction: {}", transaction.getId());

        // In production, fetch merchant's webhook URL from merchant-service
        // For demo, we'll just log it
        String webhookUrl = "http://localhost:9000/webhook"; // Dummy merchant endpoint

        try {
            log.info("Dispatching webhook to: {}", webhookUrl);
            // restTemplate.postForEntity(webhookUrl, transaction, String.class);
            log.info("Webhook successfully delivered for transaction: {}", transaction.getId());
        } catch (Exception e) {
            log.error("Failed to deliver webhook, retry logic would kick in here", e);
        }
    }
}
