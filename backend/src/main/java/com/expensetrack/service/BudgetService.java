package com.expensetrack.service;

import com.expensetrack.dto.BudgetDto;
import com.expensetrack.dto.BudgetRequest;
import com.expensetrack.entity.Budget;
import com.expensetrack.entity.User;
import com.expensetrack.repository.BudgetRepository;
import com.expensetrack.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    public BudgetDto getUserBudget() {
        User user = userService.getAuthenticatedUser();
        Budget budget = budgetRepository.findByUser(user)
                .orElseGet(() -> {
                    Budget newBudget = Budget.builder()
                            .user(user)
                            .dailyBudget(new BigDecimal("500.00"))
                            .monthlyBudget(new BigDecimal("15000.00"))
                            .build();
                    return budgetRepository.save(newBudget);
                });

        return buildBudgetDto(user, budget);
    }

    @Transactional
    public BudgetDto updateBudget(BudgetRequest request) {
        User user = userService.getAuthenticatedUser();
        Budget budget = budgetRepository.findByUser(user)
                .orElseGet(() -> Budget.builder().user(user).build());

        if (request.getDailyBudget() != null) {
            budget.setDailyBudget(request.getDailyBudget());
        }
        if (request.getMonthlyBudget() != null) {
            budget.setMonthlyBudget(request.getMonthlyBudget());
        }

        budget = budgetRepository.save(budget);
        return buildBudgetDto(user, budget);
    }

    public BudgetDto buildBudgetDto(User user, Budget budget) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        BigDecimal todayTotal = expenseRepository.sumAmountByUserAndDate(user, today);
        if (todayTotal == null) todayTotal = BigDecimal.ZERO;

        BigDecimal monthTotal = expenseRepository.sumAmountByUserAndDateRange(user, monthStart, monthEnd);
        if (monthTotal == null) monthTotal = BigDecimal.ZERO;

        BigDecimal dailyLimit = budget.getDailyBudget() != null ? budget.getDailyBudget() : BigDecimal.ZERO;
        BigDecimal monthlyLimit = budget.getMonthlyBudget() != null ? budget.getMonthlyBudget() : BigDecimal.ZERO;

        BigDecimal remainingDaily = dailyLimit.subtract(todayTotal);
        BigDecimal remainingMonthly = monthlyLimit.subtract(monthTotal);

        String dailyStatus = "NORMAL";
        if (dailyLimit.compareTo(BigDecimal.ZERO) > 0) {
            if (todayTotal.compareTo(dailyLimit) > 0) {
                dailyStatus = "EXCEEDED";
            } else if (todayTotal.compareTo(dailyLimit) == 0) {
                dailyStatus = "REACHED";
            } else if (todayTotal.compareTo(dailyLimit.multiply(new BigDecimal("0.85"))) >= 0) {
                dailyStatus = "WARNING";
            }
        }

        String monthlyStatus = "NORMAL";
        if (monthlyLimit.compareTo(BigDecimal.ZERO) > 0) {
            if (monthTotal.compareTo(monthlyLimit) > 0) {
                monthlyStatus = "EXCEEDED";
            } else if (monthTotal.compareTo(monthlyLimit) == 0) {
                monthlyStatus = "REACHED";
            } else if (monthTotal.compareTo(monthlyLimit.multiply(new BigDecimal("0.85"))) >= 0) {
                monthlyStatus = "WARNING";
            }
        }

        return BudgetDto.builder()
                .id(budget.getId())
                .dailyBudget(dailyLimit)
                .monthlyBudget(monthlyLimit)
                .todayExpenseTotal(todayTotal)
                .monthExpenseTotal(monthTotal)
                .remainingDailyBudget(remainingDaily)
                .remainingMonthlyBudget(remainingMonthly)
                .dailyStatus(dailyStatus)
                .monthlyStatus(monthlyStatus)
                .build();
    }
}
