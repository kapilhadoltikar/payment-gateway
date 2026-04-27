package com.paymentgateway.common.exception;

import com.paymentgateway.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Base class for reactive global exception handling.
 */
@Slf4j
public abstract class ReactiveBaseExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleBusinessException(BusinessException ex) {
        log.error("Reactive Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        return Mono.just(ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode())));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleValidationExceptions(WebExchangeBindException ex) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("Reactive Validation failed: {}", errors);
        return Mono.just(ResponseEntity.badRequest()
                .body(ApiResponse.error(errors, "VALIDATION_ERROR")));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleGeneralException(Exception ex) {
        log.error("Reactive Unhandled exception: ", ex);
        return Mono.just(ResponseEntity.internalServerError()
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_SERVER_ERROR")));
    }
}
