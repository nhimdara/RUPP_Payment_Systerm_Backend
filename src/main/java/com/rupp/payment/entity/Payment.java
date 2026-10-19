package com.rupp.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_status_date", columnList = "payment_status, payment_date"),
        @Index(name = "idx_payments_student", columnList = "student_id"),
        @Index(name = "idx_payments_fee_structure", columnList = "fee_structure_id"),
        @Index(name = "idx_payments_date", columnList = "payment_date")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private TuitionFeeStructure feeStructure;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "total_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPaid;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PaymentDetail> details = new ArrayList<>();

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Receipt receipt;

    public Payment() {
        this.paymentDate = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }

    public Payment(Student student, TuitionFeeStructure feeStructure, BigDecimal totalPaid, PaymentStatus status) {
        this.student = student;
        this.feeStructure = feeStructure;
        this.totalPaid = totalPaid;
        this.status = status;
        this.paymentDate = LocalDateTime.now();
    }

    public void addDetail(PaymentDetail detail) {
        details.add(detail);
        detail.setPayment(this);
    }

    public void removeDetail(PaymentDetail detail) {
        details.remove(detail);
        detail.setPayment(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public TuitionFeeStructure getFeeStructure() {
        return feeStructure;
    }

    public void setFeeStructure(TuitionFeeStructure feeStructure) {
        this.feeStructure = feeStructure;
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

    public List<PaymentDetail> getDetails() {
        return details;
    }

    public void setDetails(List<PaymentDetail> details) {
        this.details = details;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
        if (receipt != null) {
            receipt.setPayment(this);
        }
    }
}
