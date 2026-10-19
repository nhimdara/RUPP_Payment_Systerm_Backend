package com.rupp.payment.service;

import com.rupp.payment.config.PaymentSecurityProperties;
import com.rupp.payment.dto.response.OtpChallengeResponse;
import com.rupp.payment.dto.response.PaymentStatusResponse;
import com.rupp.payment.entity.AuditAction;
import com.rupp.payment.entity.OtpChallenge;
import com.rupp.payment.entity.OtpStatus;
import com.rupp.payment.exception.OtpChallengeNotFoundException;
import com.rupp.payment.exception.OtpChallengeStateException;
import com.rupp.payment.exception.OtpExpiredException;
import com.rupp.payment.exception.OtpInvalidCodeException;
import com.rupp.payment.exception.OtpLockedException;
import com.rupp.payment.exception.OtpResendCooldownException;
import com.rupp.payment.exception.PaymentNotFoundException;
import com.rupp.payment.exception.PaymentStatusConflictException;
import com.rupp.payment.repository.OtpChallengeRepository;
import com.rupp.payment.security.CurrentUserProvider;
import com.rupp.payment.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentVerificationServiceTest {

    private static final Instant START = Instant.parse("2026-10-19T08:00:00Z");
    private static final Long PAYMENT = 1001L;
    private static final Long USER = 7L;
    private static final Long STUDENT = 70L;

    @Mock
    private OtpChallengeRepository repository;
    @Mock
    private OtpSender otpSender;
    @Mock
    private PaymentOwnership ownership;
    @Mock
    private PaymentRecords paymentRecords;
    @Mock
    private PaymentAuditService auditService;
    @Mock
    private CurrentUserProvider currentUser;

    private MutableClock clock;
    private PaymentVerificationService service;
    private OtpChallenge challenge;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START);
        PaymentSecurityProperties properties = new PaymentSecurityProperties(
                new PaymentSecurityProperties.Otp(Duration.ofMinutes(5), 5, Duration.ofSeconds(60),
                        Duration.ofMinutes(15), "test-secret"),
                null);
        service = new PaymentVerificationService(repository, new OtpCodeHasher(properties), otpSender, ownership,
                paymentRecords, auditService, currentUser, clock, properties);

        when(currentUser.getUserId()).thenReturn(USER);
        when(ownership.requireOwnedPayment(PAYMENT)).thenReturn(payment(PaymentStatusName.PENDING));
        when(paymentRecords.updateStatus(PAYMENT, PaymentStatusName.PENDING, PaymentStatusName.VERIFIED)).thenReturn(true);
        when(repository.findFirstByPaymentIdAndUserIdOrderByLastSentAtDesc(PAYMENT, USER)).thenReturn(Optional.empty());
        when(repository.findByPaymentIdAndUserIdAndStatusIn(eq(PAYMENT), eq(USER), any())).thenReturn(List.of());
        when(repository.save(any(OtpChallenge.class))).thenAnswer(inv -> {
            OtpChallenge saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            challenge = saved;
            when(repository.findForUpdate(saved.getId())).thenReturn(Optional.of(saved));
            return saved;
        });
    }

    // Request

    @Test
    void requestCreatesPendingChallengeWithHashedSixDigitCode() {
        OtpChallengeResponse response = service.requestOtp(PAYMENT);
        String code = sentCode();

        assertThat(code).matches("\\d{6}");
        assertThat(challenge.getStatus()).isEqualTo(OtpStatus.PENDING);
        assertThat(challenge.getCodeHash()).isNotEqualTo(code).hasSize(64);
        assertThat(response.paymentId()).isEqualTo(PAYMENT);
        assertThat(response.expiresAt()).isEqualTo(START.plus(Duration.ofMinutes(5)));
        verify(auditService).record(eq(AuditAction.OTP_REQUESTED), eq(PAYMENT), anyString());
    }

    @Test
    void requestForSomeoneElsesPaymentIsNotFoundAndSendsNothing() {
        when(ownership.requireOwnedPayment(PAYMENT)).thenThrow(new PaymentNotFoundException());

        assertThatThrownBy(() -> service.requestOtp(PAYMENT)).isInstanceOf(PaymentNotFoundException.class);
        verify(otpSender, never()).send(any(), any(), any(), any());
    }

    @Test
    void requestForPaymentThatIsNoLongerPendingIsConflict() {
        when(ownership.requireOwnedPayment(PAYMENT)).thenReturn(payment(PaymentStatusName.VERIFIED));

        assertThatThrownBy(() -> service.requestOtp(PAYMENT))
                .isInstanceOfSatisfying(PaymentStatusConflictException.class,
                        ex -> assertThat(ex.getDetails()).containsEntry("currentStatus", PaymentStatusName.VERIFIED));
        verify(otpSender, never()).send(any(), any(), any(), any());
    }

    @Test
    void requestSupersedesOlderPendingChallenges() {
        OtpChallenge older = new OtpChallenge(PAYMENT, USER, "x", START.minusSeconds(120), START.plusSeconds(180));
        when(repository.findByPaymentIdAndUserIdAndStatusIn(eq(PAYMENT), eq(USER), any())).thenReturn(List.of(older));

        service.requestOtp(PAYMENT);

        assertThat(older.getStatus()).isEqualTo(OtpStatus.EXPIRED);
    }

    @Test
    void requestIsRejectedWhilePaymentIsLockedOut() {
        when(repository.existsByPaymentIdAndUserIdAndStatusAndLockedAtAfter(eq(PAYMENT), eq(USER), eq(OtpStatus.LOCKED), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.requestOtp(PAYMENT)).isInstanceOf(OtpLockedException.class);
        verify(otpSender, never()).send(any(), any(), any(), any());
    }

    @Test
    void newRequestWithinCooldownIsRejected() {
        service.requestOtp(PAYMENT);
        when(repository.findFirstByPaymentIdAndUserIdOrderByLastSentAtDesc(PAYMENT, USER)).thenReturn(Optional.of(challenge));
        clock.advance(Duration.ofSeconds(30));

        assertThatThrownBy(() -> service.requestOtp(PAYMENT)).isInstanceOf(OtpResendCooldownException.class);
    }

    // Verify

    @Test
    void correctCodeMovesPaymentFromPendingToVerified() {
        UUID id = service.requestOtp(PAYMENT).challengeId();

        PaymentStatusResponse response = service.verifyOtp(id, sentCode());

        assertThat(response).isEqualTo(new PaymentStatusResponse(PAYMENT, PaymentStatusName.VERIFIED));
        assertThat(challenge.getStatus()).isEqualTo(OtpStatus.VERIFIED);
        assertThat(challenge.getVerifiedAt()).isEqualTo(START);
        verify(paymentRecords).updateStatus(PAYMENT, PaymentStatusName.PENDING, PaymentStatusName.VERIFIED);
        verify(auditService).record(eq(AuditAction.OTP_VERIFIED), eq(PAYMENT), anyString());
        verify(auditService).recordStatusChange(eq(PAYMENT), eq(PaymentStatusName.PENDING), eq(PaymentStatusName.VERIFIED), anyString());
    }

    @Test
    void correctCodeForPaymentThatMovedOnIsConflictAndRetiresChallenge() {
        UUID id = service.requestOtp(PAYMENT).challengeId();
        when(paymentRecords.updateStatus(PAYMENT, PaymentStatusName.PENDING, PaymentStatusName.VERIFIED)).thenReturn(false);
        when(paymentRecords.findById(PAYMENT)).thenReturn(Optional.of(payment(PaymentStatusName.REJECTED)));

        assertThatThrownBy(() -> service.verifyOtp(id, sentCode()))
                .isInstanceOfSatisfying(PaymentStatusConflictException.class,
                        ex -> assertThat(ex.getDetails()).containsEntry("currentStatus", PaymentStatusName.REJECTED));
        assertThat(challenge.getStatus()).isEqualTo(OtpStatus.EXPIRED);
        verify(auditService, never()).recordStatusChange(any(), any(), any(), any());
    }

    @Test
    void wrongCodeCountsAttemptAndReportsRemaining() {
        UUID id = service.requestOtp(PAYMENT).challengeId();

        assertThatThrownBy(() -> service.verifyOtp(id, wrongCode()))
                .isInstanceOfSatisfying(OtpInvalidCodeException.class,
                        ex -> assertThat(ex.getDetails()).containsEntry("remainingAttempts", 4));
        assertThat(challenge.getAttempts()).isEqualTo(1);
        verify(auditService).record(eq(AuditAction.OTP_FAILED), eq(PAYMENT), anyString());
        verify(paymentRecords, never()).updateStatus(any(), any(), any());
    }

    @Test
    void fifthWrongCodeLocksChallengeAndCorrectCodeNoLongerWorks() {
        UUID id = service.requestOtp(PAYMENT).challengeId();
        String code = sentCode();
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> service.verifyOtp(id, wrongCode())).isInstanceOf(OtpInvalidCodeException.class);
        }

        assertThatThrownBy(() -> service.verifyOtp(id, wrongCode())).isInstanceOf(OtpLockedException.class);
        assertThat(challenge.getStatus()).isEqualTo(OtpStatus.LOCKED);
        assertThat(challenge.getLockedAt()).isEqualTo(START);
        verify(auditService).record(eq(AuditAction.OTP_LOCKED), eq(PAYMENT), anyString());

        assertThatThrownBy(() -> service.verifyOtp(id, code)).isInstanceOf(OtpLockedException.class);
        verify(paymentRecords, never()).updateStatus(any(), any(), any());
    }

    @Test
    void codeExpiresAfterFiveMinutes() {
        UUID id = service.requestOtp(PAYMENT).challengeId();
        String code = sentCode();
        clock.advance(Duration.ofMinutes(5));

        assertThatThrownBy(() -> service.verifyOtp(id, code)).isInstanceOf(OtpExpiredException.class);
        assertThat(challenge.getStatus()).isEqualTo(OtpStatus.EXPIRED);
        verify(auditService).record(eq(AuditAction.OTP_EXPIRED), eq(PAYMENT), anyString());
    }

    @Test
    void codeIsStillValidJustBeforeExpiry() {
        UUID id = service.requestOtp(PAYMENT).challengeId();
        String code = sentCode();
        clock.advance(Duration.ofMinutes(5).minusSeconds(1));

        assertThat(service.verifyOtp(id, code).status()).isEqualTo(PaymentStatusName.VERIFIED);
    }

    @Test
    void verifiedChallengeCannotBeVerifiedTwice() {
        UUID id = service.requestOtp(PAYMENT).challengeId();
        String code = sentCode();
        service.verifyOtp(id, code);

        assertThatThrownBy(() -> service.verifyOtp(id, code)).isInstanceOf(OtpChallengeStateException.class);
    }

    @Test
    void otherUsersChallengeIsNotFound() {
        UUID id = service.requestOtp(PAYMENT).challengeId();
        when(currentUser.getUserId()).thenReturn(99L);

        assertThatThrownBy(() -> service.verifyOtp(id, "123456")).isInstanceOf(OtpChallengeNotFoundException.class);
    }

    // Resend

    @Test
    void resendWithinSixtySecondsIsRejectedWithRetryAfter() {
        UUID id = service.requestOtp(PAYMENT).challengeId();
        clock.advance(Duration.ofSeconds(20));

        assertThatThrownBy(() -> service.resendOtp(id))
                .isInstanceOfSatisfying(OtpResendCooldownException.class,
                        ex -> assertThat(ex.getRetryAfterSeconds()).isEqualTo(40));
    }

    @Test
    void resendAfterCooldownReplacesCodeAndExtendsExpiry() {
        UUID id = service.requestOtp(PAYMENT).challengeId();
        String firstCode = sentCode();
        clock.advance(Duration.ofSeconds(60));

        OtpChallengeResponse resent = service.resendOtp(id);
        String secondCode = sentCode();

        assertThat(resent.expiresAt()).isEqualTo(START.plusSeconds(60).plus(Duration.ofMinutes(5)));
        verify(auditService).record(eq(AuditAction.OTP_RESENT), eq(PAYMENT), anyString());
        if (!firstCode.equals(secondCode)) {
            assertThatThrownBy(() -> service.verifyOtp(id, firstCode)).isInstanceOf(OtpInvalidCodeException.class);
        }
        assertThat(service.verifyOtp(id, secondCode).status()).isEqualTo(PaymentStatusName.VERIFIED);
    }

    @Test
    void resendDoesNotResetFailedAttempts() {
        UUID id = service.requestOtp(PAYMENT).challengeId();
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> service.verifyOtp(id, wrongCode())).isInstanceOf(OtpInvalidCodeException.class);
        }
        clock.advance(Duration.ofSeconds(60));
        service.resendOtp(id);

        assertThatThrownBy(() -> service.verifyOtp(id, wrongCode())).isInstanceOf(OtpLockedException.class);
    }

    private static PaymentSnapshot payment(String status) {
        return new PaymentSnapshot(PAYMENT, STUDENT, 5L, status);
    }

    private String sentCode() {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(otpSender, atLeastOnce()).send(eq(USER), eq(PAYMENT), code.capture(), any());
        return code.getValue();
    }

    private String wrongCode() {
        String code = sentCode();
        return code.equals("000000") ? "111111" : "000000";
    }
}
