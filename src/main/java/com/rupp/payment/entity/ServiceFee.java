package com.rupp.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A campus service a student can pay for outside tuition (moto parking pass, library card,
 * lab access, ID card replacement, exam retake...). Payments for these reuse the normal
 * payment / OTP / confirmation / receipt flow, linked through {@link Payment#getServiceFee()}.
 */
@Entity
@Table(name = "service_fees")
public class ServiceFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_fee_id")
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ServiceFeeCategory category;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** Human label for the period the fee covers, e.g. "Semester 1 · 2024-2025". */
    @Column(name = "validity_label", length = 100)
    private String validityLabel;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    /** One-off fees (ID card replacement, exam retake) can be paid again; passes cannot while valid. */
    @Column(name = "repeatable", nullable = false)
    private boolean repeatable = false;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public ServiceFee() {
    }

    public ServiceFee(String code, String name, ServiceFeeCategory category, String description, BigDecimal amount,
                      String validityLabel, LocalDate validFrom, LocalDate validUntil, boolean repeatable) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.validityLabel = validityLabel;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.repeatable = repeatable;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ServiceFeeCategory getCategory() { return category; }
    public void setCategory(ServiceFeeCategory category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getValidityLabel() { return validityLabel; }
    public void setValidityLabel(String validityLabel) { this.validityLabel = validityLabel; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
    public boolean isRepeatable() { return repeatable; }
    public void setRepeatable(boolean repeatable) { this.repeatable = repeatable; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
