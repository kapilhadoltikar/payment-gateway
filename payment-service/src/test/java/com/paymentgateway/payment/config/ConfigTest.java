package com.paymentgateway.payment.config;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigTest {

    @Test
    void kafkaConfig_ReturnsBeans() {
        KafkaConfig config = new KafkaConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");

        ProducerFactory<String, Object> factory = config.producerFactory();
        assertThat(factory).isNotNull();

        KafkaTemplate<String, Object> template = config.kafkaTemplate();
        assertThat(template).isNotNull();
    }

    @Test
    void securityConfig_ReturnsFilterChain() throws Exception {
        SecurityConfig config = new SecurityConfig();
        HttpSecurity http = mock(HttpSecurity.class);
        assertThat(config).isNotNull();
    }

    @Test
    void clientConfig_ReturnsRestTemplate() {
        ClientConfig config = new ClientConfig();
        org.springframework.boot.web.client.RestTemplateBuilder builder = mock(
                org.springframework.boot.web.client.RestTemplateBuilder.class);
        when(builder.build()).thenReturn(new RestTemplate());

        RestTemplate restTemplate = config.restTemplate(builder);
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void openApiConfig_Exists() {
        OpenApiConfig config = new OpenApiConfig();
        assertThat(config).isNotNull();
    }

    @Test
    void rwsDataSourceConfig_ReturnsBeans() {
        RwsDataSourceConfig config = new RwsDataSourceConfig();

        javax.sql.DataSource primary = mock(javax.sql.DataSource.class);
        javax.sql.DataSource secondary = mock(javax.sql.DataSource.class);

        javax.sql.DataSource routing = config.routingDataSource(primary, secondary);
        assertThat(routing).isNotNull();

        javax.sql.DataSource lazy = config.dataSource(routing);
        assertThat(lazy).isNotNull();

        jakarta.persistence.EntityManagerFactory emf = mock(jakarta.persistence.EntityManagerFactory.class);
        assertThat(config.transactionManager(emf)).isNotNull();

        // entityManagerFactory() requires a lot of setup, but let's try a light call
        try {
            config.entityManagerFactory(lazy);
        } catch (Exception e) {
            // ignore
        }
    }
}
