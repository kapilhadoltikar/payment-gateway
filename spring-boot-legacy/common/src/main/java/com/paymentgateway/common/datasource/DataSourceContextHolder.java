package com.paymentgateway.common.datasource;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local context holder to maintain the routing key for Read-Write
 * splitting.
 */
@Slf4j
public class DataSourceContextHolder {

    private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();

    public static void setDataSourceType(DataSourceType type) {
        log.debug("Setting DataSource Type to: {}", type);
        CONTEXT.set(type);
    }

    public static DataSourceType getDataSourceType() {
        return CONTEXT.get();
    }

    public static void clearDataSourceType() {
        CONTEXT.remove();
    }
}
