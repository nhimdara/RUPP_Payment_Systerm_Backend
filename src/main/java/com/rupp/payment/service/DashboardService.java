package com.rupp.payment.service;

import com.rupp.payment.dto.DailyRevenueDto;
import com.rupp.payment.dto.DashboardSummaryDto;
import com.rupp.payment.dto.MonthlyRevenueDto;
import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

/** Revenue aggregations for the dashboard. Only COMPLETED payments count as revenue. */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final String COMPLETED = PaymentStatus.COMPLETED.name();
    private static final long MAX_RANGE_DAYS = 366;

    private final PaymentRepository repo;

    public DashboardService(PaymentRepository repo) {
        this.repo = repo;
    }

    /** One entry per day in [from, to] (inclusive); days with no payments are 0. */
    public List<DailyRevenueDto> dailyRevenue(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must not be after 'to'");
        }
        if (from.plusDays(MAX_RANGE_DAYS).isBefore(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Date range too large (max " + MAX_RANGE_DAYS + " days)");
        }

        Map<LocalDate, BigDecimal> byDay = new HashMap<>();
        for (Object[] row : repo.dailyRevenue(COMPLETED, from.atStartOfDay(), to.plusDays(1).atStartOfDay())) {
            byDay.put(toLocalDate(row[0]), toBigDecimal(row[1]));
        }

        List<DailyRevenueDto> result = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            result.add(new DailyRevenueDto(d, byDay.getOrDefault(d, BigDecimal.ZERO)));
        }
        return result;
    }

    /** Always 12 entries (Jan-Dec); months with no payments are 0. */
    public List<MonthlyRevenueDto> monthlyRevenue(int year) {
        Map<Integer, BigDecimal> byMonth = new HashMap<>();
        for (Object[] row : repo.monthlyRevenue(COMPLETED,
                LocalDate.of(year, 1, 1).atStartOfDay(),
                LocalDate.of(year + 1, 1, 1).atStartOfDay())) {
            byMonth.put(((Number) row[0]).intValue(), toBigDecimal(row[1]));
        }

        List<MonthlyRevenueDto> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            result.add(new MonthlyRevenueDto(m,
                    Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    byMonth.getOrDefault(m, BigDecimal.ZERO)));
        }
        return result;
    }

    public DashboardSummaryDto summary() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate yearStart = today.withDayOfYear(1);

        return new DashboardSummaryDto(
                sum(today, today.plusDays(1)),
                sum(monthStart, monthStart.plusMonths(1)),
                sum(yearStart, yearStart.plusYears(1)),
                repo.countByStatus(PaymentStatus.COMPLETED),
                repo.countByStatus(PaymentStatus.PENDING),
                repo.countByStatus(PaymentStatus.FAILED));
    }

    private BigDecimal sum(LocalDate startInclusive, LocalDate endExclusive) {
        return repo.sumAmountByStatusBetween(PaymentStatus.COMPLETED,
                startInclusive.atStartOfDay(), endExclusive.atStartOfDay());
    }

    // Native queries can return different JDBC types depending on driver/Hibernate version.
    private static LocalDate toLocalDate(Object o) {
        if (o instanceof LocalDate ld) return ld;
        if (o instanceof Date d) return d.toLocalDate();
        return LocalDate.parse(o.toString().substring(0, 10));
    }

    private static BigDecimal toBigDecimal(Object o) {
        return o instanceof BigDecimal bd ? bd : new BigDecimal(o.toString());
    }
}
