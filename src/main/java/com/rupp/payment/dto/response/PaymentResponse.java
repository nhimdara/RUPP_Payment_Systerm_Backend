package com.rupp.payment.dto.response;

import com.rupp.payment.entity.Payment;
<<<<<<< HEAD
import java.math.BigDecimal;
import java.time.LocalDateTime;
=======
import com.rupp.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
>>>>>>> origin/feature/search
import java.util.List;
import java.util.stream.Collectors;

public class PaymentResponse {

    private Long id;
<<<<<<< HEAD
    private StudentResponse student;
    private TuitionFeeResponse feeStructure;
    private PaymentStatusResponse paymentStatus;
    private LocalDateTime paymentDate;
    private BigDecimal totalPaid;
    private List<PaymentDetailResponse> paymentDetails;
    private Boolean hasReceipt;
    private Long receiptId;
=======
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
>>>>>>> origin/feature/search

    public PaymentResponse() {
    }

<<<<<<< HEAD
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
=======
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

>>>>>>> origin/feature/search
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

<<<<<<< HEAD
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
=======
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
>>>>>>> origin/feature/search
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

<<<<<<< HEAD
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
=======
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
>>>>>>> origin/feature/search
    }
}
