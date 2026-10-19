package com.rupp.payment.service;

import com.rupp.payment.exception.PaymentNotFoundException;
import com.rupp.payment.exception.PaymentStatusConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentConfirmationServiceTest {

    @Mock
    private PaymentAuditService auditService;

    private InMemoryPaymentRecords payments;
    private PaymentConfirmationService service;

    @BeforeEach
    void setUp() {
        payments = new InMemoryPaymentRecords();
        service = new PaymentConfirmationService(payments, auditService);
    }

    @Test
    void verifiedPaymentCanBeConfirmed() {
        Long id = verifiedPayment();

        assertThat(service.confirm(id, null).status()).isEqualTo(PaymentStatusName.CONFIRMED);
        assertThat(payments.findById(id).orElseThrow().status()).isEqualTo(PaymentStatusName.CONFIRMED);
        verify(auditService).recordStatusChange(id, PaymentStatusName.VERIFIED, PaymentStatusName.CONFIRMED,
                "Confirmed by finance staff");
    }

    @Test
    void pendingPaymentCannotBeConfirmedBeforeStudentOtp() {
        Long id = payments.createPending(1L, 2L).paymentId();

        assertThatThrownBy(() -> service.confirm(id, "looks fine"))
                .isInstanceOfSatisfying(PaymentStatusConflictException.class,
                        ex -> assertThat(ex.getDetails()).containsEntry("currentStatus", PaymentStatusName.PENDING));
        assertThat(payments.findById(id).orElseThrow().status()).isEqualTo(PaymentStatusName.PENDING);
        verify(auditService, never()).recordStatusChange(any(), any(), any(), any());
    }

    @Test
    void confirmedPaymentCannotBeConfirmedOrRejectedAgain() {
        Long id = verifiedPayment();
        service.confirm(id, null);

        assertThatThrownBy(() -> service.confirm(id, null)).isInstanceOf(PaymentStatusConflictException.class);
        assertThatThrownBy(() -> service.reject(id, "too late")).isInstanceOf(PaymentStatusConflictException.class);
    }

    @Test
    void pendingOrVerifiedPaymentCanBeRejectedWithReason() {
        Long pending = payments.createPending(1L, 2L).paymentId();
        Long verified = verifiedPayment();

        assertThat(service.reject(pending, "Transfer not received").status()).isEqualTo(PaymentStatusName.REJECTED);
        assertThat(service.reject(verified, "Amount mismatch").status()).isEqualTo(PaymentStatusName.REJECTED);
        verify(auditService).recordStatusChange(verified, PaymentStatusName.VERIFIED, PaymentStatusName.REJECTED,
                "Amount mismatch");
    }

    @Test
    void unknownPaymentIsNotFound() {
        assertThatThrownBy(() -> service.confirm(404L, null)).isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void rejectedPaymentNoLongerCountsAsActive() {
        Long id = verifiedPayment();
        service.reject(id, "Amount mismatch");

        assertThat(payments.findActivePaymentId(1L, 3L)).isEmpty();
    }

    private Long verifiedPayment() {
        Long id = payments.createPending(1L, 3L).paymentId();
        payments.updateStatus(id, PaymentStatusName.PENDING, PaymentStatusName.VERIFIED);
        return id;
    }
}
