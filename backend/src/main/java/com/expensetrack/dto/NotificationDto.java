package com.expensetrack.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private String message;
    private String type; // BUDGET_WARNING, SPENDING_ALERT, SYSTEM
    private boolean isRead;
    private LocalDateTime createdAt;
}
