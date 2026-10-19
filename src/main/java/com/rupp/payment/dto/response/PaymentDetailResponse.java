package com.rupp.payment.dto.response;

import java.math.BigDecimal;

public class PaymentDetailResponse {
    private Long id;
    private String method;
    private String transactionRef;
    private BigDecimal amount;

    public PaymentDetailResponse() {
    }

    public PaymentDetailResponse(Long id, String method, String transactionRef, BigDecimal amount) {
        this.id = id;
        this.method = method;
        this.transactionRef = transactionRef;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
