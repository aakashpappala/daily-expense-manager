package com.expensetrack.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsReportDto {
    private List<CategoryAnalysisDto> categoryAnalyses;
    private List<TimeTrendDto> monthlyTrends;
    private List<TimeTrendDto> dailyTrends;
    private List<PaymentMethodSummaryDto> paymentMethodSummaries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryAnalysisDto {
        private Long categoryId;
        private String categoryName;
        private BigDecimal currentMonthTotal;
        private BigDecimal previousMonthTotal;
        private BigDecimal difference;
        private BigDecimal percentageChange;
        private BigDecimal dailyAverage;
        private String highestSpendingDay;
        private String lowestSpendingDay;
        private boolean isWarningAlert;
        private String warningMessage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeTrendDto {
        private String label; // "2026-08-01" or "August 2026"
        private BigDecimal amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentMethodSummaryDto {
        private String paymentMethod;
        private BigDecimal totalAmount;
        private long transactionCount;
    }
}
