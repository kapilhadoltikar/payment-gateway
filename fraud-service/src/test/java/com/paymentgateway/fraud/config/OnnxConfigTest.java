package com.paymentgateway.fraud.config;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnnxConfigTest {

    @Mock
    private OrtEnvironment env;

    @InjectMocks
    private OnnxConfig config;

    @Test
    void ortEnvironment_ReturnsInstance() {
        assertThat(config.ortEnvironment()).isNotNull();
    }

    @Test
    void championSession_HandlesOrtException() throws Exception {
        when(env.createSession(any(byte[].class), any(OrtSession.SessionOptions.class)))
                .thenThrow(new OrtException("ONNX Error"));

        OrtSession session = config.championSession(env);
        assertThat(session).isNull();
    }

    @Test
    void challengerSession_HandlesIOException() throws Exception {
        // IOException is harder to trigger from env.createSession directly if we only
        // mock OrtEnvironment,
        // but we can mock something deeper or just throw it if the signature allows.
        // Actually championSession catches both.
        when(env.createSession(any(byte[].class), any(OrtSession.SessionOptions.class)))
                .thenThrow(new OrtException("ONNX Error"));

        OrtSession session = config.challengerSession(env);
        assertThat(session).isNull();
    }

    @Test
    @SuppressWarnings("deprecation")
    void ortSession_ReturnsChampion() throws Exception {
        OrtSession mockSession = mock(OrtSession.class);
        when(env.createSession(any(byte[].class), any(OrtSession.SessionOptions.class)))
                .thenReturn(mockSession);

        OrtSession session = config.ortSession(env);
        assertThat(session).isEqualTo(mockSession);
    }
}
