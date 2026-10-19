package com.rupp.payment.service;

import com.rupp.payment.dto.response.FacultyRevenueReport;
import com.rupp.payment.dto.response.FinancialSummaryResponse;
import com.rupp.payment.dto.response.PaymentMethodReport;
import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentDetail;
import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.repository.FacultyRepository;
import com.rupp.payment.repository.PaymentDetailRepository;
import com.rupp.payment.repository.PaymentRepository;
import com.rupp.payment.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinancialReportService {

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public FinancialReportService(PaymentRepository paymentRepository,
                                  PaymentDetailRepository paymentDetailRepository,
                                  StudentRepository studentRepository,
                                  FacultyRepository facultyRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    @Transactional(readOnly = true)
    public FinancialSummaryResponse getFinancialSummary() {
        List<Payment> allPayments = paymentRepository.findAll();

        BigDecimal totalConfirmed = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;
        long confirmedCount = 0;
        long pendingCount = 0;
        long rejectedCount = 0;
        Set<Long> studentPaidIds = new HashSet<>();

        for (Payment p : allPayments) {
            String status = p.getPaymentStatus() != null ? p.getPaymentStatus().getStatusName() : "";
            BigDecimal amount = p.getTotalPaid() != null ? p.getTotalPaid() : BigDecimal.ZERO;

            if (PaymentStatus.CONFIRMED.equalsIgnoreCase(status)) {
                totalConfirmed = totalConfirmed.add(amount);
                confirmedCount++;
                if (p.getStudent() != null) {
                    studentPaidIds.add(p.getStudent().getId());
                }
            } else if (PaymentStatus.PENDING.equalsIgnoreCase(status) || PaymentStatus.VERIFIED.equalsIgnoreCase(status)) {
                totalPending = totalPending.add(amount);
                pendingCount++;
            } else if (PaymentStatus.REJECTED.equalsIgnoreCase(status)) {
                rejectedCount++;
            }
        }

        long totalStudents = studentRepository.count();

        return new FinancialSummaryResponse(
                totalConfirmed,
                totalPending,
                confirmedCount,
                pendingCount,
                rejectedCount,
                totalStudents,
                studentPaidIds.size()
        );
    }

    @Transactional(readOnly = true)
    public List<FacultyRevenueReport> getFacultyRevenueReports() {
        List<Faculty> faculties = facultyRepository.findAll();
        List<Payment> confirmedPayments = paymentRepository.findByPaymentStatusStatusName(PaymentStatus.CONFIRMED);

        Map<Long, List<Payment>> paymentsByFaculty = confirmedPayments.stream()
                .filter(p -> p.getFeeStructure() != null && p.getFeeStructure().getFaculty() != null)
                .collect(Collectors.groupingBy(p -> p.getFeeStructure().getFaculty().getId()));

        return faculties.stream().map(faculty -> {
            List<Payment> list = paymentsByFaculty.getOrDefault(faculty.getId(), Collections.emptyList());
            BigDecimal revenue = list.stream()
                    .map(Payment::getTotalPaid)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new FacultyRevenueReport(faculty.getId(), faculty.getFacultyName(), revenue, list.size());
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodReport> getPaymentMethodReports() {
        List<PaymentDetail> details = paymentDetailRepository.findAll();

        Map<String, List<PaymentDetail>> grouped = details.stream()
                .collect(Collectors.groupingBy(d -> d.getMethod() != null ? d.getMethod() : "Unknown"));

        return grouped.entrySet().stream().map(entry -> {
            BigDecimal total = entry.getValue().stream()
                    .map(PaymentDetail::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new PaymentMethodReport(entry.getKey(), total, entry.getValue().size());
        }).sorted(Comparator.comparing(PaymentMethodReport::getTotalAmount).reversed()).collect(Collectors.toList());
    }
}
