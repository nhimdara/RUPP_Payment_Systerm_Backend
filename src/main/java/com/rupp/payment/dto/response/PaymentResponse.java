package com.rupp.payment.dto.response;

import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String facultyName;
    private String academicYearLabel;
    private Long feeStructureId;
    private BigDecimal feeStructureAmount;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private BigDecimal totalPaid;
    private String remarks;
    private List<PaymentDetailResponse> details = new ArrayList<>();
    private ReceiptResponse receipt;

    public PaymentResponse() {
    }

    public static PaymentResponse from(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        if (payment.getStudent() != null) {
            response.setStudentId(payment.getStudent().getId());
            response.setStudentName(payment.getStudent().getFirstName() + " " + payment.getStudent().getLastName());
            if (payment.getStudent().getUser() != null) {
                response.setStudentEmail(payment.getStudent().getUser().getEmail());
            }
            if (payment.getStudent().getFaculty() != null) {
                response.setFacultyName(payment.getStudent().getFaculty().getFacultyName());
            }
            if (payment.getStudent().getAcademicYear() != null) {
                response.setAcademicYearLabel(payment.getStudent().getAcademicYear().getYearLabel());
            }
        }
        if (payment.getFeeStructure() != null) {
            response.setFeeStructureId(payment.getFeeStructure().getId());
            response.setFeeStructureAmount(payment.getFeeStructure().getTotalAmount());
        }
        response.setStatus(payment.getStatus());
        response.setPaymentDate(payment.getPaymentDate());
        response.setTotalPaid(payment.getTotalPaid());
        response.setRemarks(payment.getRemarks());

        if (payment.getDetails() != null) {
            response.setDetails(payment.getDetails().stream()
                    .map(d -> new PaymentDetailResponse(d.getId(), d.getMethod(), d.getTransactionRef(), d.getAmount()))
                    .collect(Collectors.toList()));
        }

        if (payment.getReceipt() != null) {
            response.setReceipt(new ReceiptResponse(
                    payment.getReceipt().getId(),
                    payment.getId(),
                    payment.getReceipt().getReceiptNumber(),
                    payment.getReceipt().getIssueDate(),
                    payment.getReceipt().getTotalAmount()
            ));
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getAcademicYearLabel() {
        return academicYearLabel;
    }

    public void setAcademicYearLabel(String academicYearLabel) {
        this.academicYearLabel = academicYearLabel;
    }

    public Long getFeeStructureId() {
        return feeStructureId;
    }

    public void setFeeStructureId(Long feeStructureId) {
        this.feeStructureId = feeStructureId;
    }

    public BigDecimal getFeeStructureAmount() {
        return feeStructureAmount;
    }

    public void setFeeStructureAmount(BigDecimal feeStructureAmount) {
        this.feeStructureAmount = feeStructureAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<PaymentDetailResponse> getDetails() {
        return details;
    }

    public void setDetails(List<PaymentDetailResponse> details) {
        this.details = details;
    }

    public ReceiptResponse getReceipt() {
        return receipt;
    }

    public void setReceipt(ReceiptResponse receipt) {
        this.receipt = receipt;
    }
}
