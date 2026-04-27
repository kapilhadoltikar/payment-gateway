package com.paymentgateway.notification.messaging;

import com.paymentgateway.common.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RabbitMQProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQProducer rabbitMQProducer;

    @Test
    public void sendWebhookTask_CallsRabbitTemplate() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());

        rabbitMQProducer.sendWebhookTask(transaction);

        verify(rabbitTemplate).convertAndSend(
                eq("webhook-exchange"),
                eq("webhook-routing-key"),
                eq(transaction));
    }
}
