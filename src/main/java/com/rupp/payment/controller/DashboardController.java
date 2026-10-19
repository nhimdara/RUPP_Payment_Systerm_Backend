package com.rupp.payment.controller;

import com.rupp.payment.dto.DailyRevenueDto;
import com.rupp.payment.dto.DashboardSummaryDto;
import com.rupp.payment.dto.MonthlyRevenueDto;
import com.rupp.payment.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** GET /api/dashboard/summary */
    @GetMapping("/summary")
    public DashboardSummaryDto summary() {
        return dashboardService.summary();
    }

    /** GET /api/dashboard/revenue/daily?from=2026-10-01&to=2026-10-31 */
    @GetMapping("/revenue/daily")
    public List<DailyRevenueDto> daily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return dashboardService.dailyRevenue(from, to);
    }

    /** GET /api/dashboard/revenue/monthly?year=2026 (year defaults to the current year) */
    @GetMapping("/revenue/monthly")
    public List<MonthlyRevenueDto> monthly(@RequestParam(required = false) Integer year) {
        return dashboardService.monthlyRevenue(year != null ? year : LocalDate.now().getYear());
    }
}
