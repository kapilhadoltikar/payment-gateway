package com.paymentgateway.notification.messaging;

import com.paymentgateway.common.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
public class RabbitMQWebhookConsumerTest {

    @InjectMocks
    private RabbitMQWebhookConsumer rabbitMQWebhookConsumer;

    @Test
    public void consume_SuccessfulDelivery() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());

        assertThatCode(() -> rabbitMQWebhookConsumer.consume(transaction))
                .doesNotThrowAnyException();
    }

    @Test
    public void consume_HandlesNullTransaction() {
        // Verify it doesn't throw NPE if transaction is null
        assertThatCode(() -> rabbitMQWebhookConsumer.consume(null))
                .doesNotThrowAnyException();
    }
}
