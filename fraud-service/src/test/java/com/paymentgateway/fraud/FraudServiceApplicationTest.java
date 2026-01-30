package com.paymentgateway.fraud;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class FraudServiceApplicationTest {

    @Test
    void main_StartsApplication() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(eq(FraudServiceApplication.class), any(String[].class)))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            FraudServiceApplication.main(new String[] {});

            mocked.verify(() -> SpringApplication.run(eq(FraudServiceApplication.class), any(String[].class)));
        }
        assertThat(new FraudServiceApplication()).isNotNull();
    }
}
