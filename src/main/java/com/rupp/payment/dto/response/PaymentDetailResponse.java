package com.rupp.payment.dto.response;

<<<<<<< HEAD
import com.rupp.payment.entity.PaymentDetail;
import java.math.BigDecimal;

public class PaymentDetailResponse {

    private Long id;
    private Long paymentId;
=======
import java.math.BigDecimal;

public class PaymentDetailResponse {
    private Long id;
>>>>>>> origin/feature/search
    private String method;
    private String transactionRef;
    private BigDecimal amount;

    public PaymentDetailResponse() {
    }

<<<<<<< HEAD
    public PaymentDetailResponse(Long id, Long paymentId, String method, String transactionRef, BigDecimal amount) {
        this.id = id;
        this.paymentId = paymentId;
=======
    public PaymentDetailResponse(Long id, String method, String transactionRef, BigDecimal amount) {
        this.id = id;
>>>>>>> origin/feature/search
        this.method = method;
        this.transactionRef = transactionRef;
        this.amount = amount;
    }

<<<<<<< HEAD
    public static PaymentDetailResponse fromEntity(PaymentDetail detail) {
        if (detail == null) return null;
        return new PaymentDetailResponse(
                detail.getId(),
                detail.getPayment() != null ? detail.getPayment().getId() : null,
                detail.getMethod(),
                detail.getTransactionRef(),
                detail.getAmount()
        );
    }

=======
>>>>>>> origin/feature/search
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

<<<<<<< HEAD
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

=======
>>>>>>> origin/feature/search
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
