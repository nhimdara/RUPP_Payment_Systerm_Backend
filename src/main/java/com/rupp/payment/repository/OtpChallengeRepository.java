package com.rupp.payment.repository;

import com.rupp.payment.entity.OtpChallenge;
import com.rupp.payment.entity.OtpStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {

    // Row lock serialises concurrent verify/resend calls so attempts can't be lost
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from OtpChallenge c where c.id = :id")
    Optional<OtpChallenge> findForUpdate(@Param("id") UUID id);

    Optional<OtpChallenge> findFirstByPaymentIdAndUserIdOrderByLastSentAtDesc(Long paymentId, Long userId);

    List<OtpChallenge> findByPaymentIdAndUserIdAndStatusIn(Long paymentId, Long userId, Collection<OtpStatus> statuses);

    boolean existsByPaymentIdAndUserIdAndStatusAndLockedAtAfter(Long paymentId, Long userId, OtpStatus status, Instant since);

    @Modifying
    @Query("update OtpChallenge c set c.status = :expired where c.status = :pending and c.expiresAt <= :now")
    int expireStale(@Param("pending") OtpStatus pending, @Param("expired") OtpStatus expired, @Param("now") Instant now);
}
