package com.rupp.payment.service;

import com.rupp.payment.config.PaymentSecurityProperties;
import com.rupp.payment.entity.AuditAction;
import com.rupp.payment.entity.IdempotencyRecord;
import com.rupp.payment.entity.IdempotencyStatus;
import com.rupp.payment.exception.IdempotencyKeyReuseException;
import com.rupp.payment.exception.IdempotencyRequestInProgressException;
import com.rupp.payment.repository.IdempotencyRecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

// Each repository call runs in its own short transaction; the unique (user_id, idempotency_key) constraint settles races
@Service
public class IdempotencyService {

    private static final int MAX_CLAIM_ATTEMPTS = 3;

    public sealed interface Outcome permits Proceed, Replay {
    }

    public record Proceed(Long recordId) implements Outcome {
    }

    public record Replay(int status, String contentType, String body) implements Outcome {
    }

    private final IdempotencyRecordRepository repository;
    private final PaymentAuditService auditService;
    private final Clock clock;
    private final Duration ttl;

    public IdempotencyService(IdempotencyRecordRepository repository, PaymentAuditService auditService,
                              Clock clock, PaymentSecurityProperties properties) {
        this.repository = repository;
        this.auditService = auditService;
        this.clock = clock;
        this.ttl = properties.idempotency().ttl();
    }

    public Outcome begin(Long userId, String key, String requestHash) {
        for (int attempt = 0; attempt < MAX_CLAIM_ATTEMPTS; attempt++) {
            Instant now = clock.instant();
            Optional<IdempotencyRecord> existing = repository.findByUserIdAndIdempotencyKey(userId, key);
            if (existing.isPresent()) {
                IdempotencyRecord record = existing.get();
                if (!record.isExpired(now)) {
                    return resolveExisting(record, key, requestHash);
                }
                repository.deleteIfExpired(record.getId(), now);
                continue;
            }
            try {
                IdempotencyRecord created = repository.saveAndFlush(
                        IdempotencyRecord.start(userId, key, requestHash, now, now.plus(ttl)));
                return new Proceed(created.getId());
            } catch (DataIntegrityViolationException concurrentInsert) {
                // another request claimed the key first; loop re-reads its record
            }
        }
        throw new IdempotencyRequestInProgressException();
    }

    public void complete(Long recordId, int responseStatus, String contentType, String body) {
        if (responseStatus >= 500) {
            release(recordId);
            return;
        }
        repository.markCompleted(recordId, IdempotencyStatus.COMPLETED, responseStatus, contentType, body);
    }

    public void release(Long recordId) {
        repository.deleteById(recordId);
    }

    public int purgeExpired() {
        return repository.deleteAllExpired(clock.instant());
    }

    private Outcome resolveExisting(IdempotencyRecord record, String key, String requestHash) {
        if (!record.getRequestHash().equals(requestHash)) {
            auditService.record(AuditAction.IDEMPOTENCY_KEY_REUSED, null, "Idempotency-Key " + key + " reused with a different request");
            throw new IdempotencyKeyReuseException();
        }
        if (record.getStatus() == IdempotencyStatus.IN_PROGRESS) {
            auditService.record(AuditAction.IDEMPOTENCY_CONFLICT, null, "Idempotency-Key " + key + " still in progress");
            throw new IdempotencyRequestInProgressException();
        }
        auditService.record(AuditAction.IDEMPOTENT_REPLAY, null, "Idempotency-Key " + key + " replayed stored response");
        return new Replay(record.getResponseStatus(), record.getResponseContentType(), record.getResponseBody());
    }
}
