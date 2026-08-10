package com.expensetrack.controller;

import com.expensetrack.dto.BudgetDto;
import com.expensetrack.dto.BudgetRequest;
import com.expensetrack.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<BudgetDto> getBudget() {
        return ResponseEntity.ok(budgetService.getUserBudget());
    }

    @PostMapping
    public ResponseEntity<BudgetDto> createOrUpdateBudget(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.updateBudget(request));
    }

    @PutMapping
    public ResponseEntity<BudgetDto> updateBudget(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.updateBudget(request));
    }
}
