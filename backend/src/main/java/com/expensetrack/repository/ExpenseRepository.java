package com.expensetrack.repository;

import com.expensetrack.entity.Category;
import com.expensetrack.entity.Expense;
import com.expensetrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserOrderByExpenseDateDescIdDesc(User user);
    Optional<Expense> findByIdAndUser(Long id, User user);

    List<Expense> findByUserAndCategoryOrderByExpenseDateDesc(User user, Category category);
    List<Expense> findByUserAndCategoryAndExpenseDateOrderByCreatedAtDesc(User user, Category category, LocalDate expenseDate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.expenseDate = :date")
    BigDecimal sumAmountByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.category = :category AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndCategoryAndDateRange(@Param("user") User user, @Param("category") Category category, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
