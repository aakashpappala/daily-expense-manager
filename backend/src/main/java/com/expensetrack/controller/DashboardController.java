package com.expensetrack.controller;

import com.expensetrack.dto.AnalyticsReportDto;
import com.expensetrack.dto.CategoryDetailDto;
import com.expensetrack.dto.DailyDetailDto;
import com.expensetrack.dto.DashboardDto;
import com.expensetrack.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboardData());
    }

    @GetMapping("/categories/{id}/details")
    public ResponseEntity<CategoryDetailDto> getCategoryDetails(
            @PathVariable Long id,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ResponseEntity.ok(dashboardService.getCategoryDetails(id, year, month));
    }

    @GetMapping("/categories/{id}/daily-details")
    public ResponseEntity<DailyDetailDto> getDailyDetails(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(dashboardService.getDailyDetails(id, date));
    }

    @GetMapping("/reports/analytics")
    public ResponseEntity<AnalyticsReportDto> getAnalyticsReport() {
        return ResponseEntity.ok(dashboardService.getAnalyticsReport());
    }
}
