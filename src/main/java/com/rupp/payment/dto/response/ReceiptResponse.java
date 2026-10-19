package com.rupp.payment.dto.response;

<<<<<<< HEAD
import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.Receipt;
=======
>>>>>>> origin/feature/search
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReceiptResponse {
<<<<<<< HEAD

=======
>>>>>>> origin/feature/search
    private Long id;
    private Long paymentId;
    private String receiptNumber;
    private LocalDateTime issueDate;
    private BigDecimal totalAmount;
<<<<<<< HEAD
    private String studentName;
    private String studentEmail;
    private String facultyName;
    private String academicYearLabel;
    private String paymentMethod;
    private String transactionRef;
=======
>>>>>>> origin/feature/search

    public ReceiptResponse() {
    }

<<<<<<< HEAD
    public ReceiptResponse(Long id, Long paymentId, String receiptNumber, LocalDateTime issueDate,
                           BigDecimal totalAmount, String studentName, String studentEmail,
                           String facultyName, String academicYearLabel, String paymentMethod,
                           String transactionRef) {
=======
    public ReceiptResponse(Long id, Long paymentId, String receiptNumber, LocalDateTime issueDate, BigDecimal totalAmount) {
>>>>>>> origin/feature/search
        this.id = id;
        this.paymentId = paymentId;
        this.receiptNumber = receiptNumber;
        this.issueDate = issueDate;
        this.totalAmount = totalAmount;
<<<<<<< HEAD
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.facultyName = facultyName;
        this.academicYearLabel = academicYearLabel;
        this.paymentMethod = paymentMethod;
        this.transactionRef = transactionRef;
    }

    public static ReceiptResponse fromEntity(Receipt receipt) {
        if (receipt == null) return null;
        Payment p = receipt.getPayment();
        String studentName = null;
        String studentEmail = null;
        String facultyName = null;
        String academicYearLabel = null;
        String method = null;
        String txnRef = null;

        if (p != null) {
            if (p.getStudent() != null) {
                studentName = p.getStudent().getFirstName() + " " + p.getStudent().getLastName();
                if (p.getStudent().getUser() != null) {
                    studentEmail = p.getStudent().getUser().getEmail();
                }
            }
            if (p.getFeeStructure() != null) {
                if (p.getFeeStructure().getFaculty() != null) {
                    facultyName = p.getFeeStructure().getFaculty().getFacultyName();
                }
                if (p.getFeeStructure().getAcademicYear() != null) {
                    academicYearLabel = p.getFeeStructure().getAcademicYear().getYearLabel();
                }
            }
            if (p.getPaymentDetails() != null && !p.getPaymentDetails().isEmpty()) {
                method = p.getPaymentDetails().get(0).getMethod();
                txnRef = p.getPaymentDetails().get(0).getTransactionRef();
            }
        }

        return new ReceiptResponse(
                receipt.getId(),
                p != null ? p.getId() : null,
                receipt.getReceiptNumber(),
                receipt.getIssueDate(),
                receipt.getTotalAmount(),
                studentName,
                studentEmail,
                facultyName,
                academicYearLabel,
                method,
                txnRef
        );
=======
>>>>>>> origin/feature/search
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
<<<<<<< HEAD

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
=======
>>>>>>> origin/feature/search
}
