package com.rupp.payment.entity;

<<<<<<< HEAD
import jakarta.persistence.*;

@Entity
@Table(name = "payment_statuses")
public class PaymentStatus {

    public static final String PENDING = "PENDING";
    public static final String VERIFIED = "VERIFIED";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String REJECTED = "REJECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_status_id")
    private Long id;

    @Column(name = "status_name", nullable = false, unique = true, length = 50)
    private String statusName;

    public PaymentStatus() {
    }

    public PaymentStatus(Long id, String statusName) {
        this.id = id;
        this.statusName = statusName;
    }

    public PaymentStatus(String statusName) {
        this.statusName = statusName;
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
=======
public enum PaymentStatus {
    PENDING,
    VERIFIED,
    CONFIRMED,
    REJECTED,
    CANCELLED,
    REFUNDED
>>>>>>> origin/feature/search
}
