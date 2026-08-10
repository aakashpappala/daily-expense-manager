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
public class CategoryDetailDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal monthlyTotal;
    private BigDecimal averageDailySpending;
    private long totalTransactions;
    private DaySpendingDto highestSpendingDay;
    private DaySpendingDto lowestSpendingDay;
    private List<DaySpendingDto> dailyBreakdown;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DaySpendingDto {
        private LocalDate date;
        private BigDecimal totalAmount;
        private long transactionCount;
    }
}
