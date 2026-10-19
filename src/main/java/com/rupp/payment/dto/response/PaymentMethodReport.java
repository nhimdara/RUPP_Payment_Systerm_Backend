package com.rupp.payment.dto.response;

import java.math.BigDecimal;

public class PaymentMethodReport {

    private String method;
    private BigDecimal totalAmount;
    private long transactionCount;

    public PaymentMethodReport() {
    }

    public PaymentMethodReport(String method, BigDecimal totalAmount, long transactionCount) {
        this.method = method;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }
}
