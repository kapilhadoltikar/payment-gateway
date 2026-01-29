package com.paymentgateway.notification.messaging;

import com.paymentgateway.common.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class KafkaPaymentConsumerTest {

    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @InjectMocks
    private KafkaPaymentConsumer kafkaPaymentConsumer;

    @Test
    public void consume_SendsWebhookTask() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());

        kafkaPaymentConsumer.consume(transaction);

        verify(rabbitMQProducer).sendWebhookTask(transaction);
    }
}
