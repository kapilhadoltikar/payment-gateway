package com.paymentgateway.common.aspect;

import com.paymentgateway.common.datasource.DataSourceContextHolder;
import com.paymentgateway.common.datasource.DataSourceType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataSourceRoutingAspectTest {

    @InjectMocks
    private DataSourceRoutingAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        DataSourceContextHolder.clearDataSourceType();
    }

    @Test
    void routeBasedOnTransaction_ReadOnly_SetsSecondary() throws Throwable {
        Method method = MockService.class.getMethod("readOnlyMethod");

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.SECONDARY);
            return "success";
        });

        Object result = aspect.routeBasedOnTransaction(joinPoint);

        assertThat(result).isEqualTo("success");
        assertThat(DataSourceContextHolder.getDataSourceType()).isNull();
    }

    @Test
    void routeBasedOnTransaction_ReadWrite_SetsPrimary() throws Throwable {
        Method method = MockService.class.getMethod("writeMethod");

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.PRIMARY);
            return "success";
        });

        Object result = aspect.routeBasedOnTransaction(joinPoint);

        assertThat(result).isEqualTo("success");
        assertThat(DataSourceContextHolder.getDataSourceType()).isNull();
    }

    @Test
    void routeBasedOnTransaction_ClassLevel_SetsPrimary() throws Throwable {
        Method method = MockClassLevelService.class.getMethod("plainMethod");

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new MockClassLevelService());
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            assertThat(DataSourceContextHolder.getDataSourceType()).isEqualTo(DataSourceType.PRIMARY);
            return "success";
        });

        Object result = aspect.routeBasedOnTransaction(joinPoint);

        assertThat(result).isEqualTo("success");
        assertThat(DataSourceContextHolder.getDataSourceType()).isNull();
    }

    @Test
    void getOrder_ReturnsHighestPrecedence() {
        assertThat(aspect.getOrder()).isEqualTo(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
    }

    static class MockService {
        @Transactional(readOnly = true)
        public void readOnlyMethod() {
        }

        @Transactional(readOnly = false)
        public void writeMethod() {
        }
    }

    @Transactional
    static class MockClassLevelService {
        public void plainMethod() {
        }
    }
}
