package com.rupp.payment.dto.request;

import com.rupp.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdatePaymentStatusRequest {

    @NotNull(message = "Target status is required")
    private PaymentStatus status;

    private String remarks;

    public UpdatePaymentStatusRequest() {
    }

    public UpdatePaymentStatusRequest(PaymentStatus status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
