package com.paymentgateway.payment.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatCode;

public class CRaCConfigTest {

    @Test
    void cracLifecycle_Works() {
        CRaCConfig config = new CRaCConfig();

        // We can't easily register with Core.getGlobalContext() in unit test
        // without complex mocking of static methods if it hits native code,
        // but let's see if we can at least call the hooks.

        assertThatCode(() -> {
            config.beforeCheckpoint(null);
            config.afterRestore(null);
            config.cleanup();
        }).doesNotThrowAnyException();
    }
}
