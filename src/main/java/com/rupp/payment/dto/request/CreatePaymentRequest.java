package com.rupp.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreatePaymentRequest {

    @NotNull(message = "Fee structure ID is required")
    private Long feeStructureId;

    @NotBlank(message = "Payment method is required (e.g., ABA, WING, ACLEDA, CASH)")
    private String paymentMethod;

    @NotBlank(message = "Transaction reference or slip number is required")
    private String transactionRef;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String remarks;

    public CreatePaymentRequest() {
    }

    public CreatePaymentRequest(Long feeStructureId, String paymentMethod, String transactionRef, BigDecimal amount, String remarks) {
        this.feeStructureId = feeStructureId;
        this.paymentMethod = paymentMethod;
        this.transactionRef = transactionRef;
        this.amount = amount;
        this.remarks = remarks;
    }

    public Long getFeeStructureId() {
        return feeStructureId;
    }

    public void setFeeStructureId(Long feeStructureId) {
        this.feeStructureId = feeStructureId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
