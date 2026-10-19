package com.rupp.payment.service;

import com.rupp.payment.entity.AuditAction;
import com.rupp.payment.exception.DuplicatePaymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicatePaymentGuardTest {

    @Mock
    private PaymentRecords paymentRecords;
    @Mock
    private PaymentAuditService auditService;

    private DuplicatePaymentGuard guard;

    @BeforeEach
    void setUp() {
        guard = new DuplicatePaymentGuard(paymentRecords, auditService);
    }

    @Test
    void allowsPaymentWhenNoActivePaymentExists() {
        when(paymentRecords.findActivePaymentId(1L, 2L)).thenReturn(Optional.empty());

        assertThatCode(() -> guard.assertNoActivePayment(1L, 2L, null)).doesNotThrowAnyException();
        verifyNoInteractions(auditService);
    }

    @Test
    void allowsRecheckingTheSamePayment() {
        when(paymentRecords.findActivePaymentId(1L, 2L)).thenReturn(Optional.of(100L));

        assertThatCode(() -> guard.assertNoActivePayment(1L, 2L, 100L)).doesNotThrowAnyException();
    }

    @Test
    void blocksSecondPaymentForSameFeeStructureAndAuditsIt() {
        when(paymentRecords.findActivePaymentId(1L, 2L)).thenReturn(Optional.of(100L));

        assertThatThrownBy(() -> guard.assertNoActivePayment(1L, 2L, 200L))
                .isInstanceOfSatisfying(DuplicatePaymentException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getDetails()).containsEntry("existingPaymentId", 100L);
                });
        verify(auditService).record(eq(AuditAction.DUPLICATE_BLOCKED), eq(200L), contains("existingPaymentId=100"));
    }

    @Test
    void duplicateBlockedIsAnIndependentAuditEvent() {
        assertThat(AuditAction.DUPLICATE_BLOCKED.isIndependent()).isTrue();
    }
}
