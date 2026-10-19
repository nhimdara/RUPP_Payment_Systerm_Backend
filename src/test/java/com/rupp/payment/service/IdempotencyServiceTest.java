package com.rupp.payment.service;

import com.rupp.payment.config.PaymentSecurityProperties;
import com.rupp.payment.entity.AuditAction;
import com.rupp.payment.entity.IdempotencyRecord;
import com.rupp.payment.entity.IdempotencyStatus;
import com.rupp.payment.exception.IdempotencyKeyReuseException;
import com.rupp.payment.exception.IdempotencyRequestInProgressException;
import com.rupp.payment.repository.IdempotencyRecordRepository;
import com.rupp.payment.service.IdempotencyService.Proceed;
import com.rupp.payment.service.IdempotencyService.Replay;
import com.rupp.payment.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-19T08:00:00Z");
    private static final Long USER = 7L;
    private static final String KEY = "3f2b7c1e-5a4d-4e6f-9b8a-1c2d3e4f5a6b";
    private static final String HASH = "hash-a";

    @Mock
    private IdempotencyRecordRepository repository;
    @Mock
    private PaymentAuditService auditService;

    private MutableClock clock;
    private IdempotencyService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        PaymentSecurityProperties properties = new PaymentSecurityProperties(null,
                new PaymentSecurityProperties.Idempotency(Duration.ofHours(24)));
        service = new IdempotencyService(repository, auditService, clock, properties);
    }

    @Test
    void newKeyIsClaimedInProgressWith24hExpiry() {
        when(repository.findByUserIdAndIdempotencyKey(USER, KEY)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(inv -> withId(inv.getArgument(0), 1L));

        IdempotencyService.Outcome outcome = service.begin(USER, KEY, HASH);

        assertThat(outcome).isEqualTo(new Proceed(1L));
        ArgumentCaptor<IdempotencyRecord> saved = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(IdempotencyStatus.IN_PROGRESS);
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(24)));
    }

    @Test
    void completedKeyWithSameBodyReplaysStoredResponse() {
        when(repository.findByUserIdAndIdempotencyKey(USER, KEY)).thenReturn(Optional.of(completed(HASH)));

        IdempotencyService.Outcome outcome = service.begin(USER, KEY, HASH);

        assertThat(outcome).isEqualTo(new Replay(201, "application/json", "{\"paymentId\":1}"));
        verify(auditService).record(eq(AuditAction.IDEMPOTENT_REPLAY), isNull(), anyString());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void sameKeyWithDifferentBodyIsRejected() {
        when(repository.findByUserIdAndIdempotencyKey(USER, KEY)).thenReturn(Optional.of(completed("other-hash")));

        assertThatThrownBy(() -> service.begin(USER, KEY, HASH)).isInstanceOf(IdempotencyKeyReuseException.class);
        verify(auditService).record(eq(AuditAction.IDEMPOTENCY_KEY_REUSED), isNull(), anyString());
    }

    @Test
    void sameKeyStillInProgressIsConflict() {
        when(repository.findByUserIdAndIdempotencyKey(USER, KEY)).thenReturn(Optional.of(inProgress(HASH)));

        assertThatThrownBy(() -> service.begin(USER, KEY, HASH)).isInstanceOf(IdempotencyRequestInProgressException.class);
    }

    @Test
    void concurrentInsertFallsBackToWinnersRecord() {
        when(repository.findByUserIdAndIdempotencyKey(USER, KEY))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(inProgress(HASH)));
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uk_idempotency_record_user_key"));

        assertThatThrownBy(() -> service.begin(USER, KEY, HASH)).isInstanceOf(IdempotencyRequestInProgressException.class);
    }

    @Test
    void expiredRecordIsDeletedAndKeyReclaimed() {
        IdempotencyRecord expired = withId(IdempotencyRecord.start(USER, KEY, "old", NOW.minus(Duration.ofHours(25)),
                NOW.minus(Duration.ofHours(1))), 5L);
        when(repository.findByUserIdAndIdempotencyKey(USER, KEY))
                .thenReturn(Optional.of(expired))
                .thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(inv -> withId(inv.getArgument(0), 6L));

        assertThat(service.begin(USER, KEY, HASH)).isEqualTo(new Proceed(6L));
        verify(repository).deleteIfExpired(5L, NOW);
    }

    @Test
    void completeStoresClientErrorsButReleasesServerErrors() {
        service.complete(1L, 409, "application/json", "{}");
        verify(repository).markCompleted(1L, IdempotencyStatus.COMPLETED, 409, "application/json", "{}");

        service.complete(2L, 500, "application/json", "{}");
        verify(repository).deleteById(2L);
        verify(repository, never()).markCompleted(eq(2L), any(), anyInt(), any(), any());
    }

    private IdempotencyRecord completed(String hash) {
        IdempotencyRecord record = inProgress(hash);
        ReflectionTestUtils.setField(record, "status", IdempotencyStatus.COMPLETED);
        ReflectionTestUtils.setField(record, "responseStatus", 201);
        ReflectionTestUtils.setField(record, "responseContentType", "application/json");
        ReflectionTestUtils.setField(record, "responseBody", "{\"paymentId\":1}");
        return record;
    }

    private IdempotencyRecord inProgress(String hash) {
        return withId(IdempotencyRecord.start(USER, KEY, hash, NOW, NOW.plus(Duration.ofHours(24))), 1L);
    }

    private static IdempotencyRecord withId(IdempotencyRecord record, Long id) {
        ReflectionTestUtils.setField(record, "id", id);
        return record;
    }
}
