package com.paymentgateway.fraud.config;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class OnnxConfig {

    @Bean
    public OrtEnvironment ortEnvironment() {
        return OrtEnvironment.getEnvironment();
    }

    @Bean
    public OrtSession ortSession(OrtEnvironment env) throws OrtException, IOException {
        // In a real scenario, the model would be loaded from a secure location or included in the resources
        // For scaffolding, we attempt to load model.onnx from resources
        try {
            byte[] modelBytes = new ClassPathResource("model.onnx").getContentAsByteArray();
            return env.createSession(modelBytes, new OrtSession.SessionOptions());
        } catch (IOException e) {
            // Fallback for development/build if model is missing
            return null;
        }
    }
}
