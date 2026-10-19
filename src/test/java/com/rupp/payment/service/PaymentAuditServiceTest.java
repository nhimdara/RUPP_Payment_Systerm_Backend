package com.rupp.payment.service;

import com.rupp.payment.entity.AuditAction;
import com.rupp.payment.entity.PaymentAuditLog;
import com.rupp.payment.repository.PaymentAuditLogRepository;
import com.rupp.payment.security.CurrentUserProvider;
import com.rupp.payment.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentAuditServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-19T08:00:00Z");

    @Mock
    private PaymentAuditLogRepository repository;
    @Mock
    private CurrentUserProvider currentUser;
    @Mock
    private PlatformTransactionManager transactionManager;

    private PaymentAuditService service;

    @BeforeEach
    void setUp() {
        service = new PaymentAuditService(repository, currentUser, transactionManager, new MutableClock(NOW));
    }

    @Test
    void failureEventIsWrittenInRequiresNewTransaction() {
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        service.record(AuditAction.OTP_FAILED, 10L, "wrong code");

        ArgumentCaptor<TransactionDefinition> definition = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(transactionManager).getTransaction(definition.capture());
        assertThat(definition.getValue().getPropagationBehavior()).isEqualTo(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        verify(repository).save(any(PaymentAuditLog.class));
    }

    @Test
    void regularEventJoinsCallerTransaction() {
        service.recordStatusChange(10L, "PENDING", "CONFIRMED", null);

        verify(transactionManager, never()).getTransaction(any());
        ArgumentCaptor<PaymentAuditLog> saved = ArgumentCaptor.forClass(PaymentAuditLog.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getAction()).isEqualTo(AuditAction.STATUS_CHANGED);
        assertThat(saved.getValue().getFromStatus()).isEqualTo("PENDING");
        assertThat(saved.getValue().getToStatus()).isEqualTo("CONFIRMED");
        assertThat(saved.getValue().getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void actorIsTakenFromCurrentUserWhenNotGiven() {
        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn(7L);
        when(currentUser.getRole()).thenReturn("ROLE_STUDENT");

        service.record(AuditAction.PAYMENT_CREATED, 10L, null);

        ArgumentCaptor<PaymentAuditLog> saved = ArgumentCaptor.forClass(PaymentAuditLog.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getActorUserId()).isEqualTo(7L);
        assertThat(saved.getValue().getActorRole()).isEqualTo("ROLE_STUDENT");
    }
}
