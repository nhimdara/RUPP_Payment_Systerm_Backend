package com.rupp.payment.repository;

import com.rupp.payment.entity.IdempotencyRecord;
import com.rupp.payment.entity.IdempotencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    @Transactional
    @Modifying
    @Query("""
            update IdempotencyRecord r
               set r.status = :status, r.responseStatus = :responseStatus,
                   r.responseContentType = :contentType, r.responseBody = :body
             where r.id = :id""")
    int markCompleted(@Param("id") Long id, @Param("status") IdempotencyStatus status,
                      @Param("responseStatus") int responseStatus, @Param("contentType") String contentType,
                      @Param("body") String body);

    @Transactional
    @Modifying
    @Query("delete from IdempotencyRecord r where r.id = :id and r.expiresAt <= :now")
    int deleteIfExpired(@Param("id") Long id, @Param("now") Instant now);

    @Transactional
    @Modifying
    @Query("delete from IdempotencyRecord r where r.expiresAt <= :now")
    int deleteAllExpired(@Param("now") Instant now);
}
