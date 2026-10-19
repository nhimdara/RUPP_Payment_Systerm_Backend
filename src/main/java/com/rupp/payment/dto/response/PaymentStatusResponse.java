package com.rupp.payment.dto.response;

import com.rupp.payment.entity.PaymentStatus;

import java.util.Objects;

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
        return new PaymentStatusResponse((long) status.ordinal(), status.name());
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

    /**
     * Compact accessor used by the payment workflow services.
     */
    public String status() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PaymentStatusResponse that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(statusName, that.statusName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, statusName);
    }
}

