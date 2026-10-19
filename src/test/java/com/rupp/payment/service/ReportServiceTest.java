package com.rupp.payment.service;

import com.rupp.payment.dto.request.ReportSearchCriteria;
import com.rupp.payment.dto.response.FinancialSummaryResponse;
import com.rupp.payment.dto.response.PagedResponse;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.entity.*;
import com.rupp.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Report Service Unit Tests")
class ReportServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("Should aggregate financial metrics correctly")
    void shouldAggregateFinancialMetricsCorrectly() {
        when(paymentRepository.sumTotalPaidByStatus(PaymentStatus.CONFIRMED)).thenReturn(new BigDecimal("1200.00"));
        when(paymentRepository.sumTotalPaidByStatus(PaymentStatus.PENDING)).thenReturn(new BigDecimal("300.00"));
        when(paymentRepository.countByStatus(PaymentStatus.CONFIRMED)).thenReturn(2L);
        when(paymentRepository.countByStatus(PaymentStatus.PENDING)).thenReturn(1L);
        when(paymentRepository.countByStatus(PaymentStatus.VERIFIED)).thenReturn(0L);
        when(paymentRepository.countByStatus(PaymentStatus.REJECTED)).thenReturn(1L);
        when(paymentRepository.countByStatus(PaymentStatus.REFUNDED)).thenReturn(0L);

        List<Object[]> facultyData = new ArrayList<>();
        facultyData.add(new Object[]{"Computer Science", 2L, new BigDecimal("1200.00")});
        when(paymentRepository.findRevenueByFaculty()).thenReturn(facultyData);

        List<Object[]> methodData = new ArrayList<>();
        methodData.add(new Object[]{"ABA_PAY", 2L, new BigDecimal("1200.00")});
        methodData.add(new Object[]{"WING", 1L, new BigDecimal("300.00")});
        when(paymentRepository.findStatsByPaymentMethod()).thenReturn(methodData);

        ReportSearchCriteria criteria = new ReportSearchCriteria();
        FinancialSummaryResponse summary = reportService.getFinancialSummary(criteria);

        assertNotNull(summary);
        assertEquals(new BigDecimal("1200.00"), summary.getTotalRevenue());
        assertEquals(new BigDecimal("300.00"), summary.getTotalPendingAmount());
        assertEquals(4L, summary.getTotalTransactions());
        assertEquals(2L, summary.getConfirmedCount());
        assertEquals(1L, summary.getPendingCount());
        assertEquals(1L, summary.getRejectedCount());

        assertTrue(summary.getFacultyBreakdown().containsKey("Computer Science"));
        assertEquals(new BigDecimal("1200.00"), summary.getFacultyBreakdown().get("Computer Science").getTotalAmount());

        assertTrue(summary.getMethodBreakdown().containsKey("ABA_PAY"));
        assertEquals(2L, summary.getMethodBreakdown().get("ABA_PAY").getCount());
    }

    @Test
    @DisplayName("Should return paginated report of payments")
    void shouldReturnPaginatedReport() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@rupp.edu.kh");

        Faculty faculty = new Faculty(1L, "IT", "Science");
        AcademicYear year = new AcademicYear(1L, "2024-2025", LocalDate.of(2024, 10, 1), LocalDate.of(2025, 7, 31));
        Student student = new Student(1L, user, "Bona", "Chan", faculty, year);
        TuitionFeeStructure fee = new TuitionFeeStructure(1L, faculty, year, null, new BigDecimal("600.00"), LocalDate.of(2025, 11, 30));

        Payment payment = new Payment(student, fee, new BigDecimal("600.00"), PaymentStatus.CONFIRMED);
        payment.setId(1L);

        Page<Payment> mockPage = new PageImpl<>(List.of(payment), PageRequest.of(0, 10), 1);
        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        ReportSearchCriteria criteria = new ReportSearchCriteria();
        criteria.setStatus(PaymentStatus.CONFIRMED);

        PagedResponse<PaymentResponse> result = reportService.getFilteredPaymentReport(criteria, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(PaymentStatus.CONFIRMED, result.getContent().get(0).getStatus());
    }
}
