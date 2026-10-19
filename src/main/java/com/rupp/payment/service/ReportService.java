package com.rupp.payment.service;

import com.rupp.payment.dto.request.PaymentSearchCriteria;
import com.rupp.payment.dto.request.ReportSearchCriteria;
import com.rupp.payment.dto.response.FinancialSummaryResponse;
import com.rupp.payment.dto.response.PagedResponse;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.repository.PaymentRepository;
import com.rupp.payment.specification.PaymentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final PaymentRepository paymentRepository;

    public ReportService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public FinancialSummaryResponse getFinancialSummary(ReportSearchCriteria criteria) {
        FinancialSummaryResponse summary = new FinancialSummaryResponse();

        // Total confirmed revenue
        BigDecimal confirmedRevenue = paymentRepository.sumTotalPaidByStatus(PaymentStatus.CONFIRMED);
        summary.setTotalRevenue(confirmedRevenue != null ? confirmedRevenue : BigDecimal.ZERO);

        // Total pending amount
        BigDecimal pendingRevenue = paymentRepository.sumTotalPaidByStatus(PaymentStatus.PENDING);
        summary.setTotalPendingAmount(pendingRevenue != null ? pendingRevenue : BigDecimal.ZERO);

        // Counts per status
        summary.setConfirmedCount(paymentRepository.countByStatus(PaymentStatus.CONFIRMED));
        summary.setPendingCount(paymentRepository.countByStatus(PaymentStatus.PENDING));
        summary.setVerifiedCount(paymentRepository.countByStatus(PaymentStatus.VERIFIED));
        summary.setRejectedCount(paymentRepository.countByStatus(PaymentStatus.REJECTED));
        summary.setRefundedCount(paymentRepository.countByStatus(PaymentStatus.REFUNDED));

        summary.setTotalTransactions(
                summary.getConfirmedCount() +
                summary.getPendingCount() +
                summary.getVerifiedCount() +
                summary.getRejectedCount() +
                summary.getRefundedCount()
        );

        // Faculty revenue breakdown
        List<Object[]> facultyData = paymentRepository.findRevenueByFaculty();
        for (Object[] row : facultyData) {
            String facultyName = (String) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal amount = (BigDecimal) row[2];
            summary.getFacultyBreakdown().put(facultyName, new FinancialSummaryResponse.FacultyStat(count, amount));
        }

        // Method breakdown
        List<Object[]> methodData = paymentRepository.findStatsByPaymentMethod();
        for (Object[] row : methodData) {
            String method = (String) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal amount = (BigDecimal) row[2];
            summary.getMethodBreakdown().put(method, new FinancialSummaryResponse.MethodStat(count, amount));
        }

        return summary;
    }

    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getFilteredPaymentReport(ReportSearchCriteria criteria, Pageable pageable) {
        PaymentSearchCriteria paymentCriteria = new PaymentSearchCriteria();
        if (criteria != null) {
            paymentCriteria.setStartDate(criteria.getStartDate());
            paymentCriteria.setEndDate(criteria.getEndDate());
            paymentCriteria.setFacultyId(criteria.getFacultyId());
            paymentCriteria.setAcademicYearId(criteria.getAcademicYearId());
            paymentCriteria.setStatus(criteria.getStatus());
        }

        Page<Payment> page = paymentRepository.findAll(PaymentSpecification.withCriteria(paymentCriteria), pageable);
        List<PaymentResponse> content = page.getContent().stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());

        return PagedResponse.from(page, content);
    }
}
