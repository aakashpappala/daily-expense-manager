package com.expensetrack.service;

import com.expensetrack.dto.*;
import com.expensetrack.entity.Category;
import com.expensetrack.entity.Expense;
import com.expensetrack.entity.User;
import com.expensetrack.exception.ResourceNotFoundException;
import com.expensetrack.repository.CategoryRepository;
import com.expensetrack.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final BudgetService budgetService;
    private final NotificationService notificationService;
    private final ExpenseService expenseService;

    public DashboardDto getDashboardData() {
        User user = userService.getAuthenticatedUser();
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        LocalDate currentMonthStart = currentMonth.atDay(1);
        LocalDate currentMonthEnd = currentMonth.atEndOfMonth();
        LocalDate previousMonthStart = previousMonth.atDay(1);
        LocalDate previousMonthEnd = previousMonth.atEndOfMonth();

        // 1. Today & Month Total
        BigDecimal todayTotal = expenseRepository.sumAmountByUserAndDate(user, today);
        if (todayTotal == null) todayTotal = BigDecimal.ZERO;

        BigDecimal monthTotal = expenseRepository.sumAmountByUserAndDateRange(user, currentMonthStart, currentMonthEnd);
        if (monthTotal == null) monthTotal = BigDecimal.ZERO;

        // 2. Budget
        BudgetDto budget = budgetService.getUserBudget();

        // 3. Category Spending Breakdown
        List<Category> userCategories = categoryRepository.findByUser(user);
        List<DashboardDto.CategorySpendingDto> categorySpendings = new ArrayList<>();
        List<String> activeAlerts = new ArrayList<>();

        for (Category category : userCategories) {
            BigDecimal currCatTotal = expenseRepository.sumAmountByUserAndCategoryAndDateRange(
                    user, category, currentMonthStart, currentMonthEnd
            );
            if (currCatTotal == null) currCatTotal = BigDecimal.ZERO;

            BigDecimal prevCatTotal = expenseRepository.sumAmountByUserAndCategoryAndDateRange(
                    user, category, previousMonthStart, previousMonthEnd
            );
            if (prevCatTotal == null) prevCatTotal = BigDecimal.ZERO;

            BigDecimal diff = currCatTotal.subtract(prevCatTotal);
            BigDecimal pct = BigDecimal.ZERO;
            boolean isHighSpending = false;

            if (prevCatTotal.compareTo(BigDecimal.ZERO) > 0) {
                pct = diff.multiply(new BigDecimal("100")).divide(prevCatTotal, 1, RoundingMode.HALF_UP);
                if (pct.compareTo(new BigDecimal("25.0")) >= 0) {
                    isHighSpending = true;
                    activeAlerts.add("⚠️ " + category.getName() + " spending increased by " + pct.toPlainString() + "% compared to last month.");
                }
            } else if (currCatTotal.compareTo(new BigDecimal("2000.00")) >= 0) {
                isHighSpending = true;
            }

            categorySpendings.add(DashboardDto.CategorySpendingDto.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .currentMonthTotal(currCatTotal)
                    .previousMonthTotal(prevCatTotal)
                    .difference(diff)
                    .percentageChange(pct)
                    .isHighSpending(isHighSpending)
                    .build());
        }

        // Sort category spendings by current month total descending
        categorySpendings.sort((a, b) -> b.getCurrentMonthTotal().compareTo(a.getCurrentMonthTotal()));

        // 4. Recent Expenses
        List<ExpenseDto> recentExpenses = expenseRepository.findByUserOrderByExpenseDateDescIdDesc(user)
                .stream()
                .limit(10)
                .map(expenseService::mapToDto)
                .collect(Collectors.toList());

        // 5. Recent Notifications
        List<NotificationDto> recentNotifications = notificationService.getUserNotifications()
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // 6. Budget Alerts
        if ("EXCEEDED".equals(budget.getDailyStatus())) {
            BigDecimal excess = todayTotal.subtract(budget.getDailyBudget());
            activeAlerts.add("⚠️ You exceeded today's budget by ₹" + excess.toPlainString());
        }
        if ("EXCEEDED".equals(budget.getMonthlyStatus())) {
            BigDecimal excess = monthTotal.subtract(budget.getMonthlyBudget());
            activeAlerts.add("⚠️ You exceeded this month's budget by ₹" + excess.toPlainString());
        }

        return DashboardDto.builder()
                .todayExpenseTotal(todayTotal)
                .monthExpenseTotal(monthTotal)
                .budget(budget)
                .categorySpendings(categorySpendings)
                .recentExpenses(recentExpenses)
                .recentNotifications(recentNotifications)
                .activeAlerts(activeAlerts)
                .build();
    }

    public CategoryDetailDto getCategoryDetails(Long categoryId, Integer year, Integer month) {
        User user = userService.getAuthenticatedUser();
        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        YearMonth targetMonth = (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserAndCategoryOrderByExpenseDateDesc(user, category)
                .stream()
                .filter(e -> !e.getExpenseDate().isBefore(monthStart) && !e.getExpenseDate().isAfter(monthEnd))
                .collect(Collectors.toList());

        BigDecimal monthlyTotal = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<LocalDate, List<Expense>> expensesByDate = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getExpenseDate, TreeMap::new, Collectors.toList()));

        List<CategoryDetailDto.DaySpendingDto> dailyBreakdown = new ArrayList<>();
        CategoryDetailDto.DaySpendingDto highestDay = null;
        CategoryDetailDto.DaySpendingDto lowestDay = null;

        for (Map.Entry<LocalDate, List<Expense>> entry : expensesByDate.entrySet()) {
            BigDecimal daySum = entry.getValue().stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CategoryDetailDto.DaySpendingDto dayDto = CategoryDetailDto.DaySpendingDto.builder()
                    .date(entry.getKey())
                    .totalAmount(daySum)
                    .transactionCount(entry.getValue().size())
                    .build();

            dailyBreakdown.add(dayDto);

            if (highestDay == null || daySum.compareTo(highestDay.getTotalAmount()) > 0) {
                highestDay = dayDto;
            }
            if (lowestDay == null || daySum.compareTo(lowestDay.getTotalAmount()) < 0) {
                lowestDay = dayDto;
            }
        }

        int daysInMonth = targetMonth.lengthOfMonth();
        BigDecimal avgDaily = monthlyTotal.divide(new BigDecimal(daysInMonth), 2, RoundingMode.HALF_UP);

        return CategoryDetailDto.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .monthlyTotal(monthlyTotal)
                .averageDailySpending(avgDaily)
                .totalTransactions(expenses.size())
                .highestSpendingDay(highestDay)
                .lowestSpendingDay(lowestDay)
                .dailyBreakdown(dailyBreakdown)
                .build();
    }

    public DailyDetailDto getDailyDetails(Long categoryId, LocalDate date) {
        User user = userService.getAuthenticatedUser();
        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        List<Expense> expenses = expenseRepository.findByUserAndCategoryAndExpenseDateOrderByCreatedAtDesc(user, category, date);

        BigDecimal dayTotal = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ExpenseDto> expenseDtos = expenses.stream()
                .map(expenseService::mapToDto)
                .collect(Collectors.toList());

        return DailyDetailDto.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .date(date)
                .dayTotal(dayTotal)
                .expenses(expenseDtos)
                .build();
    }

    public AnalyticsReportDto getAnalyticsReport() {
        User user = userService.getAuthenticatedUser();
        YearMonth currentMonth = YearMonth.now();
        YearMonth prevMonth = currentMonth.minusMonths(1);

        LocalDate currentStart = currentMonth.atDay(1);
        LocalDate currentEnd = currentMonth.atEndOfMonth();
        LocalDate prevStart = prevMonth.atDay(1);
        LocalDate prevEnd = prevMonth.atEndOfMonth();

        List<Category> categories = categoryRepository.findByUser(user);
        List<AnalyticsReportDto.CategoryAnalysisDto> categoryAnalyses = new ArrayList<>();

        for (Category cat : categories) {
            BigDecimal currSum = expenseRepository.sumAmountByUserAndCategoryAndDateRange(user, cat, currentStart, currentEnd);
            if (currSum == null) currSum = BigDecimal.ZERO;

            BigDecimal prevSum = expenseRepository.sumAmountByUserAndCategoryAndDateRange(user, cat, prevStart, prevEnd);
            if (prevSum == null) prevSum = BigDecimal.ZERO;

            BigDecimal diff = currSum.subtract(prevSum);
            BigDecimal pct = BigDecimal.ZERO;
            if (prevSum.compareTo(BigDecimal.ZERO) > 0) {
                pct = diff.multiply(new BigDecimal("100")).divide(prevSum, 1, RoundingMode.HALF_UP);
            }

            BigDecimal dailyAvg = currSum.divide(new BigDecimal(currentMonth.lengthOfMonth()), 2, RoundingMode.HALF_UP);

            CategoryDetailDto detail = getCategoryDetails(cat.getId(), currentMonth.getYear(), currentMonth.getMonthValue());

            boolean isWarning = pct.compareTo(new BigDecimal("25.0")) >= 0 && currSum.compareTo(new BigDecimal("1000.00")) >= 0;
            String warningMsg = isWarning ? "⚠️ Spending increased by " + pct.toPlainString() + "% compared to last month." : "Normal spending";

            categoryAnalyses.add(AnalyticsReportDto.CategoryAnalysisDto.builder()
                    .categoryId(cat.getId())
                    .categoryName(cat.getName())
                    .currentMonthTotal(currSum)
                    .previousMonthTotal(prevSum)
                    .difference(diff)
                    .percentageChange(pct)
                    .dailyAverage(dailyAvg)
                    .highestSpendingDay(detail.getHighestSpendingDay() != null ? detail.getHighestSpendingDay().getDate() + " (₹" + detail.getHighestSpendingDay().getTotalAmount() + ")" : "N/A")
                    .lowestSpendingDay(detail.getLowestSpendingDay() != null ? detail.getLowestSpendingDay().getDate() + " (₹" + detail.getLowestSpendingDay().getTotalAmount() + ")" : "N/A")
                    .isWarningAlert(isWarning)
                    .warningMessage(warningMsg)
                    .build());
        }

        // Daily trend for last 14 days
        List<AnalyticsReportDto.TimeTrendDto> dailyTrends = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 13; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            BigDecimal sum = expenseRepository.sumAmountByUserAndDate(user, d);
            if (sum == null) sum = BigDecimal.ZERO;
            dailyTrends.add(AnalyticsReportDto.TimeTrendDto.builder()
                    .label(d.toString())
                    .amount(sum)
                    .build());
        }

        // Monthly trend for last 6 months
        List<AnalyticsReportDto.TimeTrendDto> monthlyTrends = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentMonth.minusMonths(i);
            BigDecimal sum = expenseRepository.sumAmountByUserAndDateRange(user, ym.atDay(1), ym.atEndOfMonth());
            if (sum == null) sum = BigDecimal.ZERO;
            monthlyTrends.add(AnalyticsReportDto.TimeTrendDto.builder()
                    .label(ym.getMonth().name().substring(0, 3) + " " + ym.getYear())
                    .amount(sum)
                    .build());
        }

        // Payment method breakdown
        List<Expense> allExpenses = expenseRepository.findByUserOrderByExpenseDateDescIdDesc(user);
        Map<String, List<Expense>> byPaymentMethod = allExpenses.stream()
                .collect(Collectors.groupingBy(Expense::getPaymentMethod));

        List<AnalyticsReportDto.PaymentMethodSummaryDto> pmSummaries = new ArrayList<>();
        for (Map.Entry<String, List<Expense>> entry : byPaymentMethod.entrySet()) {
            BigDecimal sum = entry.getValue().stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            pmSummaries.add(AnalyticsReportDto.PaymentMethodSummaryDto.builder()
                    .paymentMethod(entry.getKey())
                    .totalAmount(sum)
                    .transactionCount(entry.getValue().size())
                    .build());
        }

        return AnalyticsReportDto.builder()
                .categoryAnalyses(categoryAnalyses)
                .monthlyTrends(monthlyTrends)
                .dailyTrends(dailyTrends)
                .paymentMethodSummaries(pmSummaries)
                .build();
    }
}
