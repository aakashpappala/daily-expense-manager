package com.expensetrack.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {
    private BigDecimal todayExpenseTotal;
    private BigDecimal monthExpenseTotal;
    private BudgetDto budget;
    private List<CategorySpendingDto> categorySpendings;
    private List<ExpenseDto> recentExpenses;
    private List<NotificationDto> recentNotifications;
    private List<String> activeAlerts;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySpendingDto {
        private Long categoryId;
        private String categoryName;
        private BigDecimal currentMonthTotal;
        private BigDecimal previousMonthTotal;
        private BigDecimal difference;
        private BigDecimal percentageChange;
        private boolean isHighSpending;
    }
}
