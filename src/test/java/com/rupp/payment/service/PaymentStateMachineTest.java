package com.rupp.payment.service;

import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.exception.InvalidPaymentStatusTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Status State Machine Unit Tests")
class PaymentStateMachineTest {

    private PaymentStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new PaymentStateMachine();
    }

    @ParameterizedTest(name = "Valid transition: {0} -> {1}")
    @CsvSource({
            "PENDING, VERIFIED",
            "PENDING, REJECTED",
            "PENDING, CANCELLED",
            "VERIFIED, CONFIRMED",
            "VERIFIED, REJECTED",
            "CONFIRMED, REFUNDED"
    })
    void shouldAllowValidStateTransitions(PaymentStatus current, PaymentStatus target) {
        assertTrue(stateMachine.canTransition(current, target));
        assertDoesNotThrow(() -> stateMachine.validateTransition(current, target));
    }

    @ParameterizedTest(name = "Invalid transition: {0} -> {1}")
    @CsvSource({
            "CONFIRMED, PENDING",
            "CONFIRMED, VERIFIED",
            "CONFIRMED, REJECTED",
            "CONFIRMED, CANCELLED",
            "REJECTED, PENDING",
            "REJECTED, VERIFIED",
            "REJECTED, CONFIRMED",
            "CANCELLED, PENDING",
            "CANCELLED, VERIFIED",
            "CANCELLED, CONFIRMED",
            "REFUNDED, PENDING",
            "REFUNDED, VERIFIED",
            "REFUNDED, CONFIRMED",
            "PENDING, CONFIRMED",
            "PENDING, REFUNDED",
            "VERIFIED, PENDING",
            "VERIFIED, CANCELLED",
            "VERIFIED, REFUNDED"
    })
    void shouldRejectInvalidStateTransitions(PaymentStatus current, PaymentStatus target) {
        assertFalse(stateMachine.canTransition(current, target));
        InvalidPaymentStatusTransitionException ex = assertThrows(
                InvalidPaymentStatusTransitionException.class,
                () -> stateMachine.validateTransition(current, target)
        );
        assertEquals(current, ex.getCurrentStatus());
        assertEquals(target, ex.getTargetStatus());
    }

    @Test
    @DisplayName("Should reject transition to the same status")
    void shouldRejectSelfTransition() {
        assertFalse(stateMachine.canTransition(PaymentStatus.PENDING, PaymentStatus.PENDING));
        assertThrows(InvalidPaymentStatusTransitionException.class,
                () -> stateMachine.validateTransition(PaymentStatus.PENDING, PaymentStatus.PENDING));
    }

    @Test
    @DisplayName("Should return empty transitions and false for null inputs")
    void shouldHandleNullInputsGracefully() {
        assertFalse(stateMachine.canTransition(null, PaymentStatus.PENDING));
        assertFalse(stateMachine.canTransition(PaymentStatus.PENDING, null));
        assertFalse(stateMachine.canTransition(null, null));
        assertTrue(stateMachine.getNextValidStates(null).isEmpty());
        assertFalse(stateMachine.isTerminal(null));
    }

    @Test
    @DisplayName("Should identify terminal states correctly")
    void shouldIdentifyTerminalStates() {
        assertTrue(stateMachine.isTerminal(PaymentStatus.REJECTED));
        assertTrue(stateMachine.isTerminal(PaymentStatus.CANCELLED));
        assertTrue(stateMachine.isTerminal(PaymentStatus.REFUNDED));

        assertFalse(stateMachine.isTerminal(PaymentStatus.PENDING));
        assertFalse(stateMachine.isTerminal(PaymentStatus.VERIFIED));
        assertFalse(stateMachine.isTerminal(PaymentStatus.CONFIRMED)); // can transition to REFUNDED
    }

    @Test
    @DisplayName("Should return correct next valid states for PENDING")
    void shouldReturnCorrectNextValidStatesForPending() {
        Set<PaymentStatus> nextStates = stateMachine.getNextValidStates(PaymentStatus.PENDING);
        assertEquals(3, nextStates.size());
        assertTrue(nextStates.contains(PaymentStatus.VERIFIED));
        assertTrue(nextStates.contains(PaymentStatus.REJECTED));
        assertTrue(nextStates.contains(PaymentStatus.CANCELLED));
    }
}
