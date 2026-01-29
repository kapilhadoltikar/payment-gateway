package com.paymentgateway.fraud.config;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * Configuration for Champion-Challenger ONNX models.
 * Champion (Logistic Regression) makes production decisions.
 * Challenger (XGBoost) runs in shadow mode for performance comparison.
 */
@Configuration
@Slf4j
public class OnnxConfig {

    @Bean
    public OrtEnvironment ortEnvironment() {
        return OrtEnvironment.getEnvironment();
    }

    /**
     * Champion Model: Logistic Regression (~150 KB, ~0.8ms inference)
     * Makes all production fraud decisions.
     */
    @Bean("championSession")
    @Primary
    public OrtSession championSession(OrtEnvironment env) throws OrtException, IOException {
        try {
            byte[] modelBytes = new ClassPathResource("models/logistic_regression.onnx")
                    .getContentAsByteArray();
            OrtSession session = env.createSession(modelBytes, new OrtSession.SessionOptions());
            log.info("✅ Loaded Champion model: logistic_regression.onnx (size: {} KB)",
                    modelBytes.length / 1024);
            return session;
        } catch (IOException | OrtException e) {
            log.warn("⚠️ Champion model not loaded (missing or invalid): {}", e.getMessage());
            return null;
        }
    }

    /**
     * Challenger Model: XGBoost (~65 MB, ~4.5ms inference)
     * Runs in shadow mode to prove superior performance.
     */
    @Bean("challengerSession")
    public OrtSession challengerSession(OrtEnvironment env) throws OrtException, IOException {
        try {
            byte[] modelBytes = new ClassPathResource("models/xgboost_fraud.onnx")
                    .getContentAsByteArray();
            OrtSession session = env.createSession(modelBytes, new OrtSession.SessionOptions());
            log.info("✅ Loaded Challenger model: xgboost_fraud.onnx (size: {} KB)",
                    modelBytes.length / 1024);
            return session;
        } catch (IOException | OrtException e) {
            log.warn("⚠️ Challenger model not loaded (missing or invalid): {}", e.getMessage());
            return null;
        }
    }

    /**
     * Legacy bean for backward compatibility.
     * 
     * @deprecated Use championSession or challengerSession instead.
     */
    @Bean
    @Deprecated
    public OrtSession ortSession(OrtEnvironment env) throws OrtException, IOException {
        return championSession(env);
    }
}
