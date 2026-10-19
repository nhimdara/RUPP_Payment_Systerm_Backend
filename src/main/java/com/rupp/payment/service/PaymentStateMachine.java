package com.rupp.payment.service;

import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.exception.InvalidPaymentStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PaymentStateMachine {

    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_TRANSITIONS = new EnumMap<>(PaymentStatus.class);

    static {
        // PENDING -> VERIFIED, REJECTED, CANCELLED
        VALID_TRANSITIONS.put(PaymentStatus.PENDING, EnumSet.of(
                PaymentStatus.VERIFIED,
                PaymentStatus.REJECTED,
                PaymentStatus.CANCELLED
        ));

        // VERIFIED -> CONFIRMED, REJECTED
        VALID_TRANSITIONS.put(PaymentStatus.VERIFIED, EnumSet.of(
                PaymentStatus.CONFIRMED,
                PaymentStatus.REJECTED
        ));

        // CONFIRMED -> REFUNDED
        VALID_TRANSITIONS.put(PaymentStatus.CONFIRMED, EnumSet.of(
                PaymentStatus.REFUNDED
        ));

        // Terminal states with no outgoing transitions
        VALID_TRANSITIONS.put(PaymentStatus.REJECTED, Collections.emptySet());
        VALID_TRANSITIONS.put(PaymentStatus.CANCELLED, Collections.emptySet());
        VALID_TRANSITIONS.put(PaymentStatus.REFUNDED, Collections.emptySet());
    }

    public boolean canTransition(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return false;
        }
        if (currentStatus == targetStatus) {
            return false;
        }
        Set<PaymentStatus> allowedTargets = VALID_TRANSITIONS.get(currentStatus);
        return allowedTargets != null && allowedTargets.contains(targetStatus);
    }

    public void validateTransition(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        if (!canTransition(currentStatus, targetStatus)) {
            throw new InvalidPaymentStatusTransitionException(currentStatus, targetStatus);
        }
    }

    public Set<PaymentStatus> getNextValidStates(PaymentStatus currentStatus) {
        if (currentStatus == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(VALID_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet()));
    }

    public boolean isTerminal(PaymentStatus status) {
        if (status == null) {
            return false;
        }
        Set<PaymentStatus> nextStates = VALID_TRANSITIONS.get(status);
        return nextStates == null || nextStates.isEmpty();
    }
}
