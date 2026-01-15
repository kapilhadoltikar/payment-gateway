package com.paymentgateway.common.aspect;

import com.paymentgateway.common.datasource.DataSourceContextHolder;
import com.paymentgateway.common.datasource.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aspect to route calls to the appropriate DataSource based on
 * the @Transactional(readOnly) attribute.
 * <p>
 * This aspect is ordered with HIGHEST_PRECEDENCE to ensure it runs before the
 * Spring TransactionInterceptor.
 */
@Slf4j
@Aspect
@Component
public class DataSourceRoutingAspect implements Ordered {

    @Around("@annotation(transactional)")
    public Object routeBasedOnTransaction(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        return handleRouting(joinPoint, transactional);
    }

    // Support class-level annotation if needed, but method-level usually takes
    // precedence.
    // Simplifying to handling method-level for now as it's the most common case for
    // splitting.

    private Object handleRouting(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        try {
            if (transactional.readOnly()) {
                log.debug("Routing to SECONDARY DataSource for read-only transaction: {}", joinPoint.getSignature());
                DataSourceContextHolder.setDataSourceType(DataSourceType.SECONDARY);
            } else {
                log.debug("Routing to PRIMARY DataSource for data-modifying transaction: {}", joinPoint.getSignature());
                DataSourceContextHolder.setDataSourceType(DataSourceType.PRIMARY);
            }
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
