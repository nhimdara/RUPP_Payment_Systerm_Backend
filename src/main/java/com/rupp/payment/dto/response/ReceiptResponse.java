package com.rupp.payment.dto.response;

import com.rupp.payment.entity.Receipt;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReceiptResponse {
    private Long id;
    private Long paymentId;
    private String receiptNumber;
    private LocalDateTime issueDate;
    private BigDecimal totalAmount;

    public ReceiptResponse() {
    }

    public ReceiptResponse(Long id, Long paymentId, String receiptNumber, LocalDateTime issueDate, BigDecimal totalAmount) {
        this.id = id;
        this.paymentId = paymentId;
        this.receiptNumber = receiptNumber;
        this.issueDate = issueDate;
        this.totalAmount = totalAmount;
    }

    public static ReceiptResponse fromEntity(Receipt receipt) {
        if (receipt == null) {
            return null;
        }
        return new ReceiptResponse(receipt.getId(),
                receipt.getPayment() == null ? null : receipt.getPayment().getId(),
                receipt.getReceiptNumber(), receipt.getIssueDate(), receipt.getTotalAmount());
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

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
