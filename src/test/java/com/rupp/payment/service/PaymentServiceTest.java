package com.rupp.payment.service;

import com.rupp.payment.dto.request.CreatePaymentRequest;
import com.rupp.payment.dto.request.PaymentSearchCriteria;
import com.rupp.payment.dto.request.UpdatePaymentStatusRequest;
import com.rupp.payment.dto.response.PagedResponse;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.dto.response.ReceiptResponse;
import com.rupp.payment.entity.*;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.InvalidPaymentStatusTransitionException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDetailRepository paymentDetailRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TuitionFeeStructureRepository feeStructureRepository;

    @Spy
    private PaymentStateMachine paymentStateMachine = new PaymentStateMachine();

    @InjectMocks
    private PaymentService paymentService;

    private User studentUser;
    private Student student;
    private Faculty faculty;
    private AcademicYear academicYear;
    private TuitionFeeStructure feeStructure;

    @BeforeEach
    void setUp() {
        studentUser = new User();
        studentUser.setId(10L);
        studentUser.setUsername("student1");
        studentUser.setEmail("student1@rupp.edu.kh");

        faculty = new Faculty(1L, "Computer Science", "Science");
        academicYear = new AcademicYear(1L, "2024-2025", LocalDate.of(2024, 10, 1), LocalDate.of(2025, 7, 31));

        student = new Student(1L, studentUser, "Dara", "Nhim", faculty, academicYear);

        feeStructure = new TuitionFeeStructure(1L, faculty, academicYear, null, new BigDecimal("600.00"), LocalDate.of(2025, 11, 30));
    }

    @Test
    @DisplayName("Should create payment successfully with initial PENDING status")
    void shouldCreatePaymentSuccessfully() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                1L, "ABA_PAY", "ABA-TXN-123456", new BigDecimal("600.00"), "Semester 1 payment"
        );

        when(studentRepository.findByUserId(10L)).thenReturn(Optional.of(student));
        when(feeStructureRepository.findById(1L)).thenReturn(Optional.of(feeStructure));
        when(paymentDetailRepository.existsByTransactionRef("ABA-TXN-123456")).thenReturn(false);

        Payment mockSaved = new Payment(student, feeStructure, new BigDecimal("600.00"), PaymentStatus.PENDING);
        mockSaved.setId(100L);
        mockSaved.addDetail(new PaymentDetail("ABA_PAY", "ABA-TXN-123456", new BigDecimal("600.00")));

        when(paymentRepository.save(any(Payment.class))).thenReturn(mockSaved);

        PaymentResponse response = paymentService.createPayment(studentUser, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("600.00"), response.getTotalPaid());
        assertEquals(1, response.getDetails().size());
        assertEquals("ABA-TXN-123456", response.getDetails().get(0).getTransactionRef());

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException if transactionRef already exists")
    void shouldThrowWhenDuplicateTransactionRef() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                1L, "ABA_PAY", "ABA-TXN-DUPLICATE", new BigDecimal("600.00"), null
        );

        when(studentRepository.findByUserId(10L)).thenReturn(Optional.of(student));
        when(feeStructureRepository.findById(1L)).thenReturn(Optional.of(feeStructure));
        when(paymentDetailRepository.existsByTransactionRef("ABA-TXN-DUPLICATE")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.createPayment(studentUser, request));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should transition from PENDING to VERIFIED successfully")
    void shouldTransitionPendingToVerified() {
        Payment payment = new Payment(student, feeStructure, new BigDecimal("600.00"), PaymentStatus.PENDING);
        payment.setId(100L);

        when(paymentRepository.findWithDetailsById(100L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(PaymentStatus.VERIFIED, "Proof verified");
        PaymentResponse response = paymentService.updatePaymentStatus(100L, request);

        assertEquals(PaymentStatus.VERIFIED, response.getStatus());
        assertEquals("Proof verified", response.getRemarks());
        verify(receiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should transition from VERIFIED to CONFIRMED and automatically generate Receipt")
    void shouldTransitionVerifiedToConfirmedAndGenerateReceipt() {
        Payment payment = new Payment(student, feeStructure, new BigDecimal("600.00"), PaymentStatus.VERIFIED);
        payment.setId(200L);

        when(paymentRepository.findWithDetailsById(200L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(PaymentStatus.CONFIRMED, "Payment confirmed in ABA bank");
        PaymentResponse response = paymentService.updatePaymentStatus(200L, request);

        assertEquals(PaymentStatus.CONFIRMED, response.getStatus());
        assertNotNull(response.getReceipt());
        assertTrue(response.getReceipt().getReceiptNumber().startsWith("RCP-"));
        assertEquals(new BigDecimal("600.00"), response.getReceipt().getTotalAmount());

        verify(receiptRepository, times(1)).save(any(Receipt.class));
    }

    @Test
    @DisplayName("Should throw InvalidPaymentStatusTransitionException on invalid transition (CONFIRMED -> PENDING)")
    void shouldThrowOnInvalidTransition() {
        Payment payment = new Payment(student, feeStructure, new BigDecimal("600.00"), PaymentStatus.CONFIRMED);
        payment.setId(300L);

        when(paymentRepository.findWithDetailsById(300L)).thenReturn(Optional.of(payment));

        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(PaymentStatus.PENDING, "Rollback attempt");

        assertThrows(InvalidPaymentStatusTransitionException.class,
                () -> paymentService.updatePaymentStatus(300L, request));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should search payments and return paginated response")
    void shouldSearchPaymentsWithPagination() {
        Payment payment = new Payment(student, feeStructure, new BigDecimal("600.00"), PaymentStatus.CONFIRMED);
        payment.setId(100L);

        Page<Payment> mockPage = new PageImpl<>(List.of(payment), PageRequest.of(0, 10), 1);
        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        PaymentSearchCriteria criteria = new PaymentSearchCriteria();
        criteria.setStatus(PaymentStatus.CONFIRMED);

        PagedResponse<PaymentResponse> result = paymentService.searchPayments(criteria, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(PaymentStatus.CONFIRMED, result.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("Should get receipt for confirmed payment")
    void shouldGetReceiptForConfirmedPayment() {
        Payment payment = new Payment(student, feeStructure, new BigDecimal("600.00"), PaymentStatus.CONFIRMED);
        payment.setId(100L);

        Receipt receipt = new Receipt(payment, "RCP-20261019-0001", LocalDateTime.now(), new BigDecimal("600.00"));
        receipt.setId(50L);

        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        when(receiptRepository.findByPaymentId(100L)).thenReturn(Optional.of(receipt));

        ReceiptResponse response = paymentService.getReceiptByPaymentId(100L);

        assertNotNull(response);
        assertEquals("RCP-20261019-0001", response.getReceiptNumber());
        assertEquals(new BigDecimal("600.00"), response.getTotalAmount());
    }

    @Test
    @DisplayName("Should throw BadRequestException when getting receipt for non-confirmed payment")
    void shouldThrowWhenGettingReceiptForPendingPayment() {
        Payment payment = new Payment(student, feeStructure, new BigDecimal("600.00"), PaymentStatus.PENDING);
        payment.setId(100L);

        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));

        assertThrows(BadRequestException.class, () -> paymentService.getReceiptByPaymentId(100L));
    }
}
