package com.paymentgateway.common.datasource;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class RoutingDataSourceTest {

    private final RoutingDataSource routingDataSource = new RoutingDataSource();

    @Test
    void determineCurrentLookupKey_CallsContextHolder() {
        DataSourceContextHolder.setDataSourceType(DataSourceType.PRIMARY);
        assertThat(routingDataSource.determineCurrentLookupKey()).isEqualTo(DataSourceType.PRIMARY);

        DataSourceContextHolder.setDataSourceType(DataSourceType.SECONDARY);
        assertThat(routingDataSource.determineCurrentLookupKey()).isEqualTo(DataSourceType.SECONDARY);

        DataSourceContextHolder.clearDataSourceType();
        assertThat(routingDataSource.determineCurrentLookupKey()).isNull();
    }
}
