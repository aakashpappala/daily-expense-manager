package com.expensetrack.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetDto {
    private Long id;
    private BigDecimal dailyBudget;
    private BigDecimal monthlyBudget;
    private BigDecimal todayExpenseTotal;
    private BigDecimal monthExpenseTotal;
    private BigDecimal remainingDailyBudget;
    private BigDecimal remainingMonthlyBudget;
    private String dailyStatus; // NORMAL, WARNING, EXCEEDED
    private String monthlyStatus; // NORMAL, WARNING, EXCEEDED
}
