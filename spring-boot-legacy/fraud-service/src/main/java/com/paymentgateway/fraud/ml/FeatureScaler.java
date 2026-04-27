package com.paymentgateway.fraud.ml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles Feature Scaling (StandardScaler) for AI models.
 * Loads 'mean' and 'scale' parameters exported from the Python training script.
 */
@Component
@Slf4j
public class FeatureScaler {

    private float[] mean;
    private float[] scale;
    private boolean isInitialized = false;
    private final ObjectMapper mapper;

    public FeatureScaler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        try {
            JsonNode root = mapper.readTree(new ClassPathResource("models/scaler_params.json").getInputStream());

            JsonNode meanNode = root.get("mean");
            JsonNode scaleNode = root.get("scale");

            if (meanNode != null && meanNode.isArray() && scaleNode != null && scaleNode.isArray()) {
                mean = new float[meanNode.size()];
                scale = new float[scaleNode.size()];

                for (int i = 0; i < meanNode.size(); i++) {
                    mean[i] = (float) meanNode.get(i).asDouble();
                    scale[i] = (float) scaleNode.get(i).asDouble();
                }
                isInitialized = true;
                log.info("✅ FeatureScaler initialized with {} features", mean.length);
            }
        } catch (IOException e) {
            log.warn("⚠️ scaler_params.json not found. Scaling will be skipped (using raw values).");
        }
    }

    /**
     * Apply Standard Scaling: z = (x - u) / s
     */
    public float[] scale(float[] features) {
        if (!isInitialized)
            return features;

        float[] scaled = new float[features.length];
        for (int i = 0; i < features.length; i++) {
            if (i < mean.length) {
                scaled[i] = (features[i] - mean[i]) / scale[i];
            } else {
                scaled[i] = features[i]; // No scaling for extra features
            }
        }
        return scaled;
    }
}
