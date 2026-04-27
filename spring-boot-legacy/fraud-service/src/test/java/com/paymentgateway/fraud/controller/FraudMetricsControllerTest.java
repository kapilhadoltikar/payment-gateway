package com.paymentgateway.fraud.controller;

import com.paymentgateway.fraud.model.ModelDisagreement.DisagreementType;
import com.paymentgateway.fraud.repository.ModelDisagreementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudMetricsControllerTest {

    @Mock
    private ModelDisagreementRepository repository;

    @InjectMocks
    private FraudMetricsController controller;

    @Test
    void getStats_ReturnsCorrectCalculations() {
        when(repository.count()).thenReturn(100L);
        when(repository.countByType(DisagreementType.MISSED_FRAUD)).thenReturn(5L);
        when(repository.countByType(DisagreementType.FALSE_POSITIVE)).thenReturn(10L);
        when(repository.countByType(DisagreementType.BOTH_FRAUD)).thenReturn(20L);
        when(repository.countByType(DisagreementType.BOTH_LEGIT)).thenReturn(65L);

        FraudMetricsController.DisagreementStats stats = controller.getStats();

        assertThat(stats.getTotal()).isEqualTo(100L);
        assertThat(stats.getMissedFraud()).isEqualTo(5L);
        assertThat(stats.getFalsePositive()).isEqualTo(10L);
        assertThat(stats.getBothFraud()).isEqualTo(20L);
        assertThat(stats.getBothLegit()).isEqualTo(65L);
        assertThat(stats.getDisagreementRate()).isEqualTo(15.0);
    }

    @Test
    void getStats_ZeroTotal_ReturnsZeroRate() {
        when(repository.count()).thenReturn(0L);

        FraudMetricsController.DisagreementStats stats = controller.getStats();

        assertThat(stats.getDisagreementRate()).isEqualTo(0.0);
    }
}
