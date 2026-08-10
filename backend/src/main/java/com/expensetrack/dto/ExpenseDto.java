package com.expensetrack.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDto {
    private Long id;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String description;
    private String paymentMethod;
    private String location;
    private String billPath;
    private String billFileName;
    private String billFileType;
    private boolean hasBill;
    private CategoryDto category;
    private LocalDateTime createdAt;
}
