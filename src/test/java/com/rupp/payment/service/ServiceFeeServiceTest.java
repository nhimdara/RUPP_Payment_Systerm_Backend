package com.rupp.payment.service;

import com.rupp.payment.dto.request.PayServiceFeeRequest;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.dto.response.ServiceFeeResponse;
import com.rupp.payment.entity.*;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ConflictException;
import com.rupp.payment.repository.PaymentDetailRepository;
import com.rupp.payment.repository.PaymentRepository;
import com.rupp.payment.repository.ServiceFeeRepository;
import com.rupp.payment.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Campus Service Fee Unit Tests")
class ServiceFeeServiceTest {

    @Mock private ServiceFeeRepository serviceFeeRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentDetailRepository paymentDetailRepository;
    @Mock private StudentRepository studentRepository;

    @InjectMocks private ServiceFeeService service;

    private User user;
    private Student student;
    private ServiceFee parking;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(10L);
        user.setUsername("student1");
        Faculty faculty = new Faculty(1L, "Computer Science", "Science");
        AcademicYear year = new AcademicYear(1L, "2024-2025", LocalDate.of(2024, 10, 1), LocalDate.of(2025, 7, 31));
        student = new Student(1L, user, "Dara", "Nhim", faculty, year);

        parking = new ServiceFee("MOTO-SEM", "Moto Parking Pass (Semester)", ServiceFeeCategory.MOTO_PARKING,
                "Parking", new BigDecimal("15.00"), "This semester",
                LocalDate.now().minusDays(1), LocalDate.now().plusMonths(5), false);
        parking.setId(7L);

        lenient().when(studentRepository.findByUserId(10L)).thenReturn(Optional.of(student));
        lenient().when(serviceFeeRepository.findById(7L)).thenReturn(Optional.of(parking));
    }

    private Payment payment(PaymentStatus status) {
        Payment p = new Payment();
        p.setId(99L);
        p.setStudent(student);
        p.setServiceFee(parking);
        p.setTotalPaid(parking.getAmount());
        p.setStatus(status);
        p.setPaymentDate(LocalDateTime.now());
        return p;
    }

    @Test
    @DisplayName("Paying creates a PENDING service payment for the fee amount, with no tuition fee structure")
    void payCreatesPendingServicePayment() {
        when(paymentRepository.findByStudentIdAndServiceFeeId(1L, 7L)).thenReturn(List.of());
        when(paymentDetailRepository.existsByTransactionRef("KHQR-1")).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(55L);
            return p;
        });

        PaymentResponse r = service.pay(user, 7L, new PayServiceFeeRequest("khqr_bakong", "KHQR-1", null));

        assertEquals(PaymentStatus.PENDING, r.getStatus());
        assertEquals(0, new BigDecimal("15.00").compareTo(r.getTotalPaid()));
        assertEquals("SERVICE", r.getPaymentType());
        assertEquals(7L, r.getServiceFeeId());
        assertNull(r.getFeeStructureId());
        assertEquals("KHQR_BAKONG", r.getDetails().get(0).getMethod());
    }

    @Test
    @DisplayName("A second payment while one is pending is rejected")
    void payRejectsWhileInProgress() {
        when(paymentRepository.findByStudentIdAndServiceFeeId(1L, 7L)).thenReturn(List.of(payment(PaymentStatus.PENDING)));
        assertThrows(ConflictException.class,
                () -> service.pay(user, 7L, new PayServiceFeeRequest("WING", "W-1", null)));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("A pass that is already confirmed cannot be bought twice")
    void payRejectsAlreadyPaidPass() {
        when(paymentRepository.findByStudentIdAndServiceFeeId(1L, 7L)).thenReturn(List.of(payment(PaymentStatus.CONFIRMED)));
        assertThrows(ConflictException.class,
                () -> service.pay(user, 7L, new PayServiceFeeRequest("WING", "W-2", null)));
    }

    @Test
    @DisplayName("An expired fee period cannot be paid")
    void payRejectsExpiredFee() {
        parking.setValidUntil(LocalDate.now().minusDays(1));
        assertThrows(BadRequestException.class,
                () -> service.pay(user, 7L, new PayServiceFeeRequest("WING", "W-3", null)));
    }

    @Test
    @DisplayName("My fees report PAID for a confirmed pass and NOT_PAID otherwise")
    void listForStudentReportsStatus() {
        ServiceFee library = new ServiceFee("LIB-YEAR", "Library Pass", ServiceFeeCategory.LIBRARY,
                null, new BigDecimal("10.00"), null, null, null, false);
        library.setId(8L);
        when(serviceFeeRepository.findByActiveTrueOrderByCategoryAscNameAsc()).thenReturn(List.of(parking, library));
        when(paymentRepository.findByStudentIdAndServiceFeeIsNotNull(1L)).thenReturn(List.of(payment(PaymentStatus.CONFIRMED)));

        List<ServiceFeeResponse> fees = service.listForStudent(user);

        assertEquals("PAID", fees.get(0).myStatus());
        assertEquals(99L, fees.get(0).myPaymentId());
        assertEquals("NOT_PAID", fees.get(1).myStatus());
    }
}
