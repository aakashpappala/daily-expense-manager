package com.expensetrack.service;

import com.expensetrack.dto.ExpenseDto;
import com.expensetrack.entity.Category;
import com.expensetrack.entity.Expense;
import com.expensetrack.entity.User;
import com.expensetrack.repository.BudgetRepository;
import com.expensetrack.repository.CategoryRepository;
import com.expensetrack.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserService userService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ExpenseService expenseService;

    private User userA;
    private User userB;
    private Category rapidoCategory;

    @BeforeEach
    void setUp() {
        userA = User.builder().id(1L).fullName("User A").email("usera@example.com").build();
        userB = User.builder().id(2L).fullName("User B").email("userb@example.com").build();

        rapidoCategory = Category.builder().id(10L).name("Rapido").isDefault(true).user(userA).build();
    }

    @Test
    void addRapidoExpense_OverloadedMethod_Success() {
        when(userService.getAuthenticatedUser()).thenReturn(userA);
        when(categoryRepository.findByNameAndUser("Rapido", userA)).thenReturn(Optional.of(rapidoCategory));

        Expense savedExpense = Expense.builder()
                .id(100L)
                .amount(new BigDecimal("180.00"))
                .description("College ki vellanu")
                .location("Bhimavaram")
                .expenseDate(LocalDate.now())
                .paymentMethod("UPI")
                .user(userA)
                .category(rapidoCategory)
                .build();

        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        ExpenseDto result = expenseService.addRapidoExpense(new BigDecimal("180.00"), "College ki vellanu", "Bhimavaram");

        assertNotNull(result);
        assertEquals(new BigDecimal("180.00"), result.getAmount());
        assertEquals("College ki vellanu", result.getDescription());
        assertEquals("Bhimavaram", result.getLocation());
    }

    @Test
    void getUserExpenses_DataIsolationVerified() {
        when(userService.getAuthenticatedUser()).thenReturn(userA);

        Expense expenseA = Expense.builder()
                .id(1L)
                .amount(new BigDecimal("500.00"))
                .user(userA)
                .category(rapidoCategory)
                .build();

        when(expenseRepository.findByUserOrderByExpenseDateDescIdDesc(userA)).thenReturn(List.of(expenseA));

        List<ExpenseDto> result = expenseService.getUserExpenses();

        assertEquals(1, result.size());
        verify(expenseRepository, times(1)).findByUserOrderByExpenseDateDescIdDesc(userA);
        verify(expenseRepository, never()).findByUserOrderByExpenseDateDescIdDesc(userB);
    }
}
