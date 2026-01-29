package com.paymentgateway.gateway.exception;

import com.paymentgateway.common.exception.ReactiveBaseExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GatewayExceptionHandler extends ReactiveBaseExceptionHandler {
}
