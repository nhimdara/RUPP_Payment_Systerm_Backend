package com.rupp.payment.service;

import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.entity.Receipt;
import com.rupp.payment.repository.PaymentRepository;
import com.rupp.payment.repository.ReceiptRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment records: receipts on confirmation")
class JpaPaymentRecordsTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private ReceiptRepository receiptRepository;
    @InjectMocks private JpaPaymentRecords records;

    private Payment payment(PaymentStatus status) {
        Payment p = new Payment();
        p.setId(12L);
        p.setStatus(status);
        p.setTotalPaid(new BigDecimal("15.00"));
        return p;
    }

    @Test
    @DisplayName("VERIFIED -> CONFIRMED issues an official receipt for the paid amount")
    void confirmIssuesReceipt() {
        Payment p = payment(PaymentStatus.VERIFIED);
        when(paymentRepository.findById(12L)).thenReturn(Optional.of(p));
        when(receiptRepository.findByPaymentId(12L)).thenReturn(Optional.empty());

        assertTrue(records.updateStatus(12L, "VERIFIED", "CONFIRMED"));

        ArgumentCaptor<Receipt> captor = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository).save(captor.capture());
        assertTrue(captor.getValue().getReceiptNumber().startsWith("RCP-"));
        assertEquals(0, new BigDecimal("15.00").compareTo(captor.getValue().getTotalAmount()));
        assertSame(captor.getValue(), p.getReceipt());
    }

    @Test
    @DisplayName("Rejecting does not issue a receipt")
    void rejectIssuesNoReceipt() {
        when(paymentRepository.findById(12L)).thenReturn(Optional.of(payment(PaymentStatus.PENDING)));

        assertTrue(records.updateStatus(12L, "PENDING", "REJECTED"));
        verify(receiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("A stale expected status changes nothing")
    void staleStatusIsRejected() {
        when(paymentRepository.findById(12L)).thenReturn(Optional.of(payment(PaymentStatus.CONFIRMED)));

        assertFalse(records.updateStatus(12L, "VERIFIED", "CONFIRMED"));
        verify(paymentRepository, never()).save(any());
        verify(receiptRepository, never()).save(any());
    }
}
