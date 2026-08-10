package com.expensetrack.service;

import com.expensetrack.dto.ExpenseDto;
import com.expensetrack.dto.ExpenseRequest;
import com.expensetrack.dto.RapidoExpenseRequest;
import com.expensetrack.entity.Budget;
import com.expensetrack.entity.Category;
import com.expensetrack.entity.Expense;
import com.expensetrack.entity.User;
import com.expensetrack.exception.ApiException;
import com.expensetrack.exception.ResourceNotFoundException;
import com.expensetrack.repository.BudgetRepository;
import com.expensetrack.repository.CategoryRepository;
import com.expensetrack.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final CategoryService categoryService;

    public List<ExpenseDto> getUserExpenses() {
        User user = userService.getAuthenticatedUser();
        return expenseRepository.findByUserOrderByExpenseDateDescIdDesc(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ExpenseDto getExpenseById(Long id) {
        User user = userService.getAuthenticatedUser();
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
        return mapToDto(expense);
    }

    @Transactional
    public ExpenseDto createExpense(ExpenseRequest request, MultipartFile billFile) {
        User user = userService.getAuthenticatedUser();
        Category category = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        String storedFileName = null;
        String originalFileName = null;
        String contentType = null;

        if (billFile != null && !billFile.isEmpty()) {
            originalFileName = billFile.getOriginalFilename();
            contentType = billFile.getContentType();
            storedFileName = fileStorageService.storeFile(billFile, user.getId());
        }

        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .description(request.getDescription())
                .paymentMethod(request.getPaymentMethod())
                .location(request.getLocation())
                .billPath(storedFileName)
                .billFileName(originalFileName)
                .billFileType(contentType)
                .user(user)
                .category(category)
                .build();

        expense = expenseRepository.save(expense);

        // Perform Budget and Alert Checks
        checkBudgetAndTriggerAlerts(user, category, expense.getAmount(), expense.getExpenseDate());

        return mapToDto(expense);
    }

    // =========================================================================
    // RAPIDO SPECIAL REQUIREMENT - DEMONSTRATING JAVA METHOD OVERLOADING
    // =========================================================================

    /**
     * Overloaded Method 1: Add Rapido expense with amount only.
     */
    @Transactional
    public ExpenseDto addRapidoExpense(BigDecimal amount) {
        return addRapidoExpense(amount, null, null, LocalDate.now(), "UPI");
    }

    /**
     * Overloaded Method 2: Add Rapido expense with amount and message.
     */
    @Transactional
    public ExpenseDto addRapidoExpense(BigDecimal amount, String message) {
        return addRapidoExpense(amount, message, null, LocalDate.now(), "UPI");
    }

    /**
     * Overloaded Method 3: Add Rapido expense with amount, message, and location.
     */
    @Transactional
    public ExpenseDto addRapidoExpense(BigDecimal amount, String message, String location) {
        return addRapidoExpense(amount, message, location, LocalDate.now(), "UPI");
    }

    /**
     * Overloaded Method 4: Master Rapido method with full details.
     */
    @Transactional
    public ExpenseDto addRapidoExpense(BigDecimal amount, String message, String location, LocalDate expenseDate, String paymentMethod) {
        User user = userService.getAuthenticatedUser();

        // Get or create user Rapido category
        Category rapidoCategory = categoryRepository.findByNameAndUser("Rapido", user)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name("Rapido")
                        .isDefault(true)
                        .user(user)
                        .build()));

        Expense expense = Expense.builder()
                .amount(amount)
                .expenseDate(expenseDate != null ? expenseDate : LocalDate.now())
                .description(message != null ? message : "Rapido Ride")
                .paymentMethod(paymentMethod != null ? paymentMethod : "UPI")
                .location(location)
                .user(user)
                .category(rapidoCategory)
                .build();

        expense = expenseRepository.save(expense);
        checkBudgetAndTriggerAlerts(user, rapidoCategory, expense.getAmount(), expense.getExpenseDate());
        return mapToDto(expense);
    }

    /**
     * Helper to process Rapido DTO request delegating to appropriate overloaded method.
     */
    @Transactional
    public ExpenseDto processRapidoRequest(RapidoExpenseRequest request) {
        LocalDate date = request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now();
        String pm = request.getPaymentMethod() != null ? request.getPaymentMethod() : "UPI";

        if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
            return addRapidoExpense(request.getAmount(), request.getMessage(), request.getLocation(), date, pm);
        } else if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            return addRapidoExpense(request.getAmount(), request.getMessage(), null, date, pm);
        } else {
            return addRapidoExpense(request.getAmount());
        }
    }

    // =========================================================================

    @Transactional
    public ExpenseDto updateExpense(Long id, ExpenseRequest request, MultipartFile billFile) {
        User user = userService.getAuthenticatedUser();
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));

        Category category = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        if (billFile != null && !billFile.isEmpty()) {
            // Delete old file if exists
            fileStorageService.deleteFile(expense.getBillPath());
            String storedFileName = fileStorageService.storeFile(billFile, user.getId());
            expense.setBillPath(storedFileName);
            expense.setBillFileName(billFile.getOriginalFilename());
            expense.setBillFileType(billFile.getContentType());
        }

        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setDescription(request.getDescription());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setLocation(request.getLocation());
        expense.setCategory(category);

        expense = expenseRepository.save(expense);
        checkBudgetAndTriggerAlerts(user, category, expense.getAmount(), expense.getExpenseDate());
        return mapToDto(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        User user = userService.getAuthenticatedUser();
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));

        fileStorageService.deleteFile(expense.getBillPath());
        expenseRepository.delete(expense);
    }

    @Transactional
    public ExpenseDto attachBill(Long expenseId, MultipartFile file) {
        User user = userService.getAuthenticatedUser();
        Expense expense = expenseRepository.findByIdAndUser(expenseId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        fileStorageService.deleteFile(expense.getBillPath());
        String storedFileName = fileStorageService.storeFile(file, user.getId());
        expense.setBillPath(storedFileName);
        expense.setBillFileName(file.getOriginalFilename());
        expense.setBillFileType(file.getContentType());

        expense = expenseRepository.save(expense);
        return mapToDto(expense);
    }

    @Transactional
    public ExpenseDto deleteBill(Long expenseId) {
        User user = userService.getAuthenticatedUser();
        Expense expense = expenseRepository.findByIdAndUser(expenseId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        fileStorageService.deleteFile(expense.getBillPath());
        expense.setBillPath(null);
        expense.setBillFileName(null);
        expense.setBillFileType(null);

        expense = expenseRepository.save(expense);
        return mapToDto(expense);
    }

    private void checkBudgetAndTriggerAlerts(User user, Category category, BigDecimal newAmount, LocalDate expenseDate) {
        try {
            // 1. Daily Budget Check
            BigDecimal todayTotal = expenseRepository.sumAmountByUserAndDate(user, expenseDate);
            if (todayTotal == null) todayTotal = BigDecimal.ZERO;

            Budget budget = budgetRepository.findByUser(user).orElse(null);
            if (budget != null && budget.getDailyBudget() != null && budget.getDailyBudget().compareTo(BigDecimal.ZERO) > 0) {
                if (todayTotal.compareTo(budget.getDailyBudget()) > 0) {
                    BigDecimal excess = todayTotal.subtract(budget.getDailyBudget());
                    notificationService.createNotification(
                            user,
                            "⚠️ Daily budget exceeded by ₹" + excess.toPlainString(),
                            "BUDGET_WARNING"
                    );
                } else if (todayTotal.compareTo(budget.getDailyBudget()) == 0) {
                    notificationService.createNotification(
                            user,
                            "⚠️ Daily budget of ₹" + budget.getDailyBudget().toPlainString() + " has been reached.",
                            "BUDGET_WARNING"
                    );
                }
            }

            // 2. Monthly Budget Check
            YearMonth currentMonth = YearMonth.from(expenseDate);
            BigDecimal monthTotal = expenseRepository.sumAmountByUserAndDateRange(user, currentMonth.atDay(1), currentMonth.atEndOfMonth());
            if (monthTotal == null) monthTotal = BigDecimal.ZERO;

            if (budget != null && budget.getMonthlyBudget() != null && budget.getMonthlyBudget().compareTo(BigDecimal.ZERO) > 0) {
                if (monthTotal.compareTo(budget.getMonthlyBudget()) > 0) {
                    BigDecimal excess = monthTotal.subtract(budget.getMonthlyBudget());
                    notificationService.createNotification(
                            user,
                            "⚠️ Monthly budget exceeded by ₹" + excess.toPlainString(),
                            "BUDGET_WARNING"
                    );
                }
            }

            // 3. Category Spending Analysis vs Previous Month
            YearMonth prevMonth = currentMonth.minusMonths(1);
            BigDecimal currentCategoryTotal = expenseRepository.sumAmountByUserAndCategoryAndDateRange(
                    user, category, currentMonth.atDay(1), currentMonth.atEndOfMonth()
            );
            BigDecimal prevCategoryTotal = expenseRepository.sumAmountByUserAndCategoryAndDateRange(
                    user, category, prevMonth.atDay(1), prevMonth.atEndOfMonth()
            );

            if (currentCategoryTotal != null && prevCategoryTotal != null && prevCategoryTotal.compareTo(BigDecimal.ZERO) > 0) {
                if (currentCategoryTotal.compareTo(prevCategoryTotal) > 0) {
                    BigDecimal diff = currentCategoryTotal.subtract(prevCategoryTotal);
                    BigDecimal pct = diff.multiply(new BigDecimal("100")).divide(prevCategoryTotal, 1, RoundingMode.HALF_UP);
                    if (pct.compareTo(new BigDecimal("25.0")) >= 0) {
                        notificationService.createNotification(
                                user,
                                "⚠️ " + category.getName() + " spending is " + pct.toPlainString() + "% higher than last month.",
                                "SPENDING_ALERT"
                        );
                    }
                }
            }
        } catch (Exception e) {
            // Ensure alert checks do not abort transaction
        }
    }

    public ExpenseDto mapToDto(Expense expense) {
        return ExpenseDto.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .description(expense.getDescription())
                .paymentMethod(expense.getPaymentMethod())
                .location(expense.getLocation())
                .billPath(expense.getBillPath())
                .billFileName(expense.getBillFileName())
                .billFileType(expense.getBillFileType())
                .hasBill(expense.getBillPath() != null && !expense.getBillPath().isEmpty())
                .category(categoryService.mapToDto(expense.getCategory()))
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
