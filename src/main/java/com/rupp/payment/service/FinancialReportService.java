package com.rupp.payment.service;

import com.rupp.payment.dto.response.DailyRevenueReport;
import com.rupp.payment.dto.response.FacultyRevenueReport;
import com.rupp.payment.dto.response.FinancialSummaryResponse;
import com.rupp.payment.dto.response.MonthlyRevenueReport;
import com.rupp.payment.dto.response.PaymentMethodReport;
import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentDetail;
import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.repository.FacultyRepository;
import com.rupp.payment.repository.PaymentDetailRepository;
import com.rupp.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FinancialReportService {

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final FacultyRepository facultyRepository;

    public FinancialReportService(PaymentRepository paymentRepository,
                                  PaymentDetailRepository paymentDetailRepository,
                                  FacultyRepository facultyRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.facultyRepository = facultyRepository;
    }

    @Transactional(readOnly = true)
    public FinancialSummaryResponse getFinancialSummary() {
        FinancialSummaryResponse summary = new FinancialSummaryResponse();
        summary.setTotalRevenue(value(paymentRepository.sumTotalPaidByStatus(PaymentStatus.CONFIRMED)));
        summary.setTotalPendingAmount(value(paymentRepository.sumTotalPaidByStatus(PaymentStatus.PENDING))
                .add(value(paymentRepository.sumTotalPaidByStatus(PaymentStatus.VERIFIED))));
        summary.setConfirmedCount(paymentRepository.countByStatus(PaymentStatus.CONFIRMED));
        summary.setPendingCount(paymentRepository.countByStatus(PaymentStatus.PENDING));
        summary.setVerifiedCount(paymentRepository.countByStatus(PaymentStatus.VERIFIED));
        summary.setRejectedCount(paymentRepository.countByStatus(PaymentStatus.REJECTED));
        summary.setRefundedCount(paymentRepository.countByStatus(PaymentStatus.REFUNDED));
        summary.setTotalTransactions(summary.getConfirmedCount() + summary.getPendingCount()
                + summary.getVerifiedCount() + summary.getRejectedCount() + summary.getRefundedCount());

        List<Payment> confirmedPayments = paymentRepository.findByStatus(PaymentStatus.CONFIRMED);
        LocalDate today = LocalDate.now();

        BigDecimal todayRev = confirmedPayments.stream()
                .filter(p -> p.getPaymentDate() != null && p.getPaymentDate().toLocalDate().equals(today))
                .map(Payment::getTotalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTodayRevenue(todayRev);

        BigDecimal currentMonthRev = confirmedPayments.stream()
                .filter(p -> p.getPaymentDate() != null
                        && p.getPaymentDate().getYear() == today.getYear()
                        && p.getPaymentDate().getMonth() == today.getMonth())
                .map(Payment::getTotalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setCurrentMonthRevenue(currentMonthRev);

        summary.setDailyBreakdown(getDailyRevenueReports(today.minusDays(14), today));
        summary.setMonthlyBreakdown(getMonthlyRevenueReports(today.getYear()));

        return summary;
    }

    @Transactional(readOnly = true)
    public List<DailyRevenueReport> getDailyRevenueReports(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(23, 59, 59);

        List<Payment> payments = paymentRepository.findByStatusAndPaymentDateBetween(
                PaymentStatus.CONFIRMED, startDt, endDt);

        Map<LocalDate, List<Payment>> grouped = payments.stream()
                .filter(p -> p.getPaymentDate() != null)
                .collect(Collectors.groupingBy(p -> p.getPaymentDate().toLocalDate()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    BigDecimal total = entry.getValue().stream()
                            .map(Payment::getTotalPaid)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new DailyRevenueReport(entry.getKey(), total, entry.getValue().size());
                })
                .sorted(java.util.Comparator.comparing(DailyRevenueReport::getDate))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MonthlyRevenueReport> getMonthlyRevenueReports(Integer targetYear) {
        int year = targetYear != null ? targetYear : LocalDate.now().getYear();

        LocalDateTime startDt = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime endDt = LocalDate.of(year, 12, 31).atTime(23, 59, 59);

        List<Payment> payments = paymentRepository.findByStatusAndPaymentDateBetween(
                PaymentStatus.CONFIRMED, startDt, endDt);

        Map<Integer, List<Payment>> grouped = payments.stream()
                .filter(p -> p.getPaymentDate() != null)
                .collect(Collectors.groupingBy(p -> p.getPaymentDate().getMonthValue()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    int monthVal = entry.getKey();
                    String yearMonth = String.format("%04d-%02d", year, monthVal);
                    String monthName = java.time.Month.of(monthVal).name();
                    BigDecimal total = entry.getValue().stream()
                            .map(Payment::getTotalPaid)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new MonthlyRevenueReport(yearMonth, year, monthVal, monthName, total, entry.getValue().size());
                })
                .sorted(java.util.Comparator.comparingInt(MonthlyRevenueReport::getMonth))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FacultyRevenueReport> getFacultyRevenueReports() {
        Map<Long, List<Payment>> byFaculty = paymentRepository.findAll().stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.CONFIRMED)
                .collect(Collectors.groupingBy(payment -> payment.getFeeStructure().getFaculty().getId()));
        return facultyRepository.findAll().stream().map(faculty -> toFacultyReport(faculty, byFaculty.get(faculty.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodReport> getPaymentMethodReports() {
        return paymentDetailRepository.findAll().stream()
                .collect(Collectors.groupingBy(PaymentDetail::getMethod))
                .entrySet().stream()
                .map(entry -> new PaymentMethodReport(entry.getKey(), entry.getValue().stream()
                        .map(PaymentDetail::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add), entry.getValue().size()))
                .collect(Collectors.toList());
    }

    private static FacultyRevenueReport toFacultyReport(Faculty faculty, List<Payment> payments) {
        List<Payment> safePayments = payments == null ? List.of() : payments;
        BigDecimal revenue = safePayments.stream().map(Payment::getTotalPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FacultyRevenueReport(faculty.getId(), faculty.getFacultyName(), revenue, safePayments.size());
    }

    private static BigDecimal value(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
