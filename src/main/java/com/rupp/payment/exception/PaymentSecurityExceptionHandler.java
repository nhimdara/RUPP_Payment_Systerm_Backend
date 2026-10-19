package com.rupp.payment.exception;

import com.rupp.payment.dto.response.ApiResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// Scoped to payment-security exceptions; ordered first so GlobalExceptionHandler's catch-all can't turn them into 500s
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PaymentSecurityExceptionHandler {

    @ExceptionHandler(PaymentSecurityException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handle(PaymentSecurityException ex) {
        ResponseEntity.BodyBuilder response = ResponseEntity.status(ex.getStatus());
        if (ex instanceof OtpResendCooldownException cooldown) {
            response.header(HttpHeaders.RETRY_AFTER, String.valueOf(cooldown.getRetryAfterSeconds()));
        }
        Map<String, Object> details = ex.getDetails().isEmpty() ? null : ex.getDetails();
        return response.body(new ApiResponse<>(false, ex.getMessage(), details));
    }
}
