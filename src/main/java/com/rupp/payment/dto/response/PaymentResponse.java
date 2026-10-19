package com.rupp.payment.dto.response;

import com.rupp.payment.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentResponse {

    private Long id;
    private StudentResponse student;
    private TuitionFeeResponse feeStructure;
    private PaymentStatusResponse paymentStatus;
    private LocalDateTime paymentDate;
    private BigDecimal totalPaid;
    private List<PaymentDetailResponse> paymentDetails;
    private Boolean hasReceipt;
    private Long receiptId;

    public PaymentResponse() {
    }

    public PaymentResponse(Long id, StudentResponse student, TuitionFeeResponse feeStructure,
                           PaymentStatusResponse paymentStatus, LocalDateTime paymentDate,
                           BigDecimal totalPaid, List<PaymentDetailResponse> paymentDetails,
                           Boolean hasReceipt, Long receiptId) {
        this.id = id;
        this.student = student;
        this.feeStructure = feeStructure;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
        this.totalPaid = totalPaid;
        this.paymentDetails = paymentDetails;
        this.hasReceipt = hasReceipt;
        this.receiptId = receiptId;
    }

    public static PaymentResponse fromEntity(Payment payment) {
        if (payment == null) return null;
        List<PaymentDetailResponse> details = null;
        if (payment.getPaymentDetails() != null) {
            details = payment.getPaymentDetails().stream()
                    .map(PaymentDetailResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return new PaymentResponse(
                payment.getId(),
                payment.getStudent() != null ? StudentResponse.fromEntity(payment.getStudent()) : null,
                payment.getFeeStructure() != null ? TuitionFeeResponse.fromEntity(payment.getFeeStructure()) : null,
                payment.getPaymentStatus() != null ? PaymentStatusResponse.fromEntity(payment.getPaymentStatus()) : null,
                payment.getPaymentDate(),
                payment.getTotalPaid(),
                details,
                false,
                null
        );
    }

    public static PaymentResponse fromEntity(Payment payment, Long receiptId) {
        PaymentResponse response = fromEntity(payment);
        if (response != null && receiptId != null) {
            response.setHasReceipt(true);
            response.setReceiptId(receiptId);
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentResponse getStudent() {
        return student;
    }

    public void setStudent(StudentResponse student) {
        this.student = student;
    }

    public TuitionFeeResponse getFeeStructure() {
        return feeStructure;
    }

    public void setFeeStructure(TuitionFeeResponse feeStructure) {
        this.feeStructure = feeStructure;
    }

    public PaymentStatusResponse getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatusResponse paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public List<PaymentDetailResponse> getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(List<PaymentDetailResponse> paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public Boolean getHasReceipt() {
        return hasReceipt;
    }

    public void setHasReceipt(Boolean hasReceipt) {
        this.hasReceipt = hasReceipt;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }
}
