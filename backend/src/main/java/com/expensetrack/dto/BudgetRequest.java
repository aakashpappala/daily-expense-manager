package com.expensetrack.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRequest {

    @DecimalMin(value = "0.00", message = "Daily budget must be non-negative")
    private BigDecimal dailyBudget;

    @DecimalMin(value = "0.00", message = "Monthly budget must be non-negative")
    private BigDecimal monthlyBudget;
}
