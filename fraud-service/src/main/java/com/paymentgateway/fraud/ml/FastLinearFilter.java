package com.paymentgateway.fraud.ml;

import org.springframework.stereotype.Component;

/**
 * Tier 1: Fast Linear Filter (Pure Java Logistic Regression).
 * <p>
 * This class implements a logistic regression model manually to avoid
 * the overhead of native JNI calls (ONNX Runtime) for clear-cut cases.
 * It serves as a "Fast Path" to filter out obvious approvals and blocks.
 * <p>
 * Logic: Sigmoid(dot_product(weights, features) + bias)
 */
@Component
public class FastLinearFilter {

    // Hypothetical weights trained via Scikit-Learn (Logistic Regression)
    // Features: [Amount, Velocity, IsNight, AmountDelta, NewDevice, ...zeros]
    // These weights bias towards blocking high velocity and new devices slightly.
    private final double[] weights = {
            0.0005, // Weight for Amount (small impact per dollar)
            0.85, // Weight for Velocity (high impact)
            0.40, // Weight for IsNight
            0.02, // Weight for AmountDelta
            1.50, // Weight for NewDevice (high risk)
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0 // Padding
    };

    private final double bias = -4.5; // Bias to shift curve (baseline approval)

    /**
     * Predict fraud probability using Logistic Regression formula.
     *
     * @param features Input feature vector
     * @return Probability between 0.0 and 1.0
     */
    public double predict(float[] features) {
        double z = bias;

        // Dot product
        for (int i = 0; i < weights.length && i < features.length; i++) {
            z += weights[i] * features[i];
        }

        // Sigmoid function: 1 / (1 + e^-z)
        return 1.0 / (1.0 + Math.exp(-z));
    }
}
