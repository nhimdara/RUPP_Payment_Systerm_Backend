package com.rupp.payment.dto.response;

import com.rupp.payment.entity.PaymentStatus;

public class PaymentStatusResponse {

    private Long id;
    private String statusName;

    public PaymentStatusResponse() {
    }

    public PaymentStatusResponse(Long id, String statusName) {
        this.id = id;
        this.statusName = statusName;
    }

    public static PaymentStatusResponse fromEntity(PaymentStatus status) {
        if (status == null) return null;
        return new PaymentStatusResponse(status.getId(), status.getStatusName());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
