package com.rupp.payment.dto.response;

import com.rupp.payment.entity.PaymentDetail;
import java.math.BigDecimal;

public class PaymentDetailResponse {

    private Long id;
    private Long paymentId;
    private String method;
    private String transactionRef;
    private BigDecimal amount;

    public PaymentDetailResponse() {
    }

    public PaymentDetailResponse(Long id, Long paymentId, String method, String transactionRef, BigDecimal amount) {
        this.id = id;
        this.paymentId = paymentId;
        this.method = method;
        this.transactionRef = transactionRef;
        this.amount = amount;
    }

    public static PaymentDetailResponse fromEntity(PaymentDetail detail) {
        if (detail == null) return null;
        return new PaymentDetailResponse(
                detail.getId(),
                detail.getPayment() != null ? detail.getPayment().getId() : null,
                detail.getMethod(),
                detail.getTransactionRef(),
                detail.getAmount()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
