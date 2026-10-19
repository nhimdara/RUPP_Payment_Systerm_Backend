package com.rupp.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class MakePaymentRequest {

    @NotNull(message = "Fee structure ID is required")
    @Schema(description = "ID of the tuition fee structure being paid", example = "1")
    private Long feeStructureId;

    @NotBlank(message = "Payment method is required")
    @Schema(description = "Payment method (e.g., ABA PayWay, KHQR, Bank Transfer, Cash)", example = "ABA PayWay")
    private String method;

    @NotBlank(message = "Transaction reference or receipt proof is required")
    @Schema(description = "Transaction reference or transaction hash from the payment gateway", example = "ABA-TXN-20261019-98213")
    private String transactionRef;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    @Schema(description = "Amount paid", example = "600.00")
    private BigDecimal amount;

    public MakePaymentRequest() {
    }

    public MakePaymentRequest(Long feeStructureId, String method, String transactionRef, BigDecimal amount) {
        this.feeStructureId = feeStructureId;
        this.method = method;
        this.transactionRef = transactionRef;
        this.amount = amount;
    }

    public Long getFeeStructureId() {
        return feeStructureId;
    }

    public void setFeeStructureId(Long feeStructureId) {
        this.feeStructureId = feeStructureId;
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
