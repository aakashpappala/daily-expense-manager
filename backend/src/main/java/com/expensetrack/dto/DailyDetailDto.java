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
public class DailyDetailDto {
    private Long categoryId;
    private String categoryName;
    private LocalDate date;
    private BigDecimal dayTotal;
    private List<ExpenseDto> expenses;
}
