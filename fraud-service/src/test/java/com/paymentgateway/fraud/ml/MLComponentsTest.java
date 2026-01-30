package com.paymentgateway.fraud.ml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MLComponentsTest {

    @Mock
    private ObjectMapper mapper;

    @Test
    void featureScaler_Uninitialized_ReturnsRaw() {
        FeatureScaler scaler = new FeatureScaler(mapper);
        float[] features = { 100.0f };
        float[] scaled = scaler.scale(features);
        assertThat(scaled).isSameAs(features);
    }

    @Test
    void featureScaler_InitAndScale_Works() throws Exception {
        FeatureScaler scaler = new FeatureScaler(mapper);

        ObjectMapper realMapper = new ObjectMapper();
        ObjectNode root = realMapper.createObjectNode();
        ArrayNode mean = root.putArray("mean");
        mean.add(50.0);
        ArrayNode scale = root.putArray("scale");
        scale.add(10.0);

        when(mapper.readTree(any(InputStream.class))).thenReturn(root);

        scaler.init();

        float[] features = { 100.0f, 5.0f };
        float[] scaled = scaler.scale(features);

        assertThat(scaled[0]).isEqualTo(5.0f); // (100 - 50) / 10
        assertThat(scaled[1]).isEqualTo(5.0f); // extra feature remains raw
    }

    @Test
    void fastLinearFilter_Works() {
        FastLinearFilter filter = new FastLinearFilter();
        float[] features = { 100.0f, 10.0f, 1.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
        double score = filter.predict(features);

        assertThat(score).isBetween(0.0, 1.0);
    }
}
