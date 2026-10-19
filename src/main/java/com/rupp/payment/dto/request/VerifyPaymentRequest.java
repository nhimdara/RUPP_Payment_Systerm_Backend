package com.rupp.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public class VerifyPaymentRequest {

    @Schema(description = "Optional notes or remarks during verification", example = "Transaction ref verified with ABA merchant portal")
    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;

    public VerifyPaymentRequest() {
    }

    public VerifyPaymentRequest(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
