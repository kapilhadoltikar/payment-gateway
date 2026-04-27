package com.paymentgateway.common.datasource;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class DataSourceContextHolderTest {

    @Test
    void setAndGet_WorksCorrectly() {
        DataSourceContextHolder.setDataSourceType(DataSourceType.PRIMARY);
        assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.PRIMARY);

        DataSourceContextHolder.setDataSourceType(DataSourceType.SECONDARY);
        assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.SECONDARY);
    }

    @Test
    void clear_WorksCorrectly() {
        DataSourceContextHolder.setDataSourceType(DataSourceType.PRIMARY);
        DataSourceContextHolder.clearDataSourceType();
        assertThat(DataSourceContextHolder.getDataSourceType()).isNull();
    }
}
