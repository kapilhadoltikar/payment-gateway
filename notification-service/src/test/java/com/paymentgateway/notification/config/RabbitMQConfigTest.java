package com.paymentgateway.notification.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

public class RabbitMQConfigTest {

    private final RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();

    @Test
    public void queue_IsDurable() {
        Queue queue = rabbitMQConfig.queue();
        assertThat(queue.getName()).isEqualTo("webhook-queue");
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    public void exchange_IsTopic() {
        TopicExchange exchange = rabbitMQConfig.exchange();
        assertThat(exchange.getName()).isEqualTo("notification-exchange");
    }

    @Test
    public void binding_IsCorrect() {
        Queue queue = rabbitMQConfig.queue();
        TopicExchange exchange = rabbitMQConfig.exchange();
        Binding binding = rabbitMQConfig.binding(queue, exchange);

        assertThat(binding.getDestination()).isEqualTo("webhook-queue");
        assertThat(binding.getExchange()).isEqualTo("notification-exchange");
        assertThat(binding.getRoutingKey()).isEqualTo("notification.#");
    }

    @Test
    public void messageConverter_IsJackson() {
        MessageConverter converter = rabbitMQConfig.jsonMessageConverter();
        assertThat(converter).isNotNull();
    }
}
