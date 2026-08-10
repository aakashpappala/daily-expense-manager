package com.expensetrack.controller;

import com.expensetrack.dto.ExpenseDto;
import com.expensetrack.dto.ExpenseRequest;
import com.expensetrack.dto.RapidoExpenseRequest;
import com.expensetrack.entity.User;
import com.expensetrack.service.ExpenseService;
import com.expensetrack.service.FileStorageService;
import com.expensetrack.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getExpenses(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String query
    ) {
        List<ExpenseDto> expenses = expenseService.getUserExpenses();

        if (categoryId != null) {
            expenses = expenses.stream()
                    .filter(e -> e.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            expenses = expenses.stream()
                    .filter(e -> !e.getExpenseDate().isBefore(start))
                    .collect(Collectors.toList());
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            LocalDate end = LocalDate.parse(endDate);
            expenses = expenses.stream()
                    .filter(e -> !e.getExpenseDate().isAfter(end))
                    .collect(Collectors.toList());
        }

        if (paymentMethod != null && !paymentMethod.trim().isEmpty() && !"ALL".equalsIgnoreCase(paymentMethod)) {
            expenses = expenses.stream()
                    .filter(e -> paymentMethod.equalsIgnoreCase(e.getPaymentMethod()))
                    .collect(Collectors.toList());
        }

        if (query != null && !query.trim().isEmpty()) {
            String q = query.toLowerCase().trim();
            expenses = expenses.stream()
                    .filter(e -> (e.getDescription() != null && e.getDescription().toLowerCase().contains(q))
                            || (e.getLocation() != null && e.getLocation().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpense(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ExpenseDto> createExpense(
            @Valid @RequestPart("expense") ExpenseRequest request,
            @RequestPart(value = "bill", required = false) MultipartFile bill
    ) {
        return ResponseEntity.ok(expenseService.createExpense(request, bill));
    }

    @PostMapping(value = "/json", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<ExpenseDto> createExpenseJson(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.createExpense(request, null));
    }

    @PostMapping("/rapido")
    public ResponseEntity<ExpenseDto> addRapidoExpense(@Valid @RequestBody RapidoExpenseRequest request) {
        return ResponseEntity.ok(expenseService.processRapidoRequest(request));
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ExpenseDto> updateExpense(
            @PathVariable Long id,
            @Valid @RequestPart("expense") ExpenseRequest request,
            @RequestPart(value = "bill", required = false) MultipartFile bill
    ) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request, bill));
    }

    @PutMapping(value = "/{id}/json", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<ExpenseDto> updateExpenseJson(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(Map.of("message", "Expense deleted successfully"));
    }

    @PostMapping("/{id}/bill")
    public ResponseEntity<ExpenseDto> attachBill(@PathVariable Long id, @RequestParam("bill") MultipartFile file) {
        return ResponseEntity.ok(expenseService.attachBill(id, file));
    }

    @DeleteMapping("/{id}/bill")
    public ResponseEntity<ExpenseDto> deleteBill(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.deleteBill(id));
    }

    @GetMapping("/{id}/bill/download")
    public ResponseEntity<Resource> downloadBill(@PathVariable Long id) {
        ExpenseDto expense = expenseService.getExpenseById(id);
        if (!expense.isHasBill()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = fileStorageService.loadFileAsResource(expense.getBillFileName());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + expense.getBillFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/bill/view")
    public ResponseEntity<Resource> viewBill(@PathVariable Long id) {
        ExpenseDto expense = expenseService.getExpenseById(id);
        if (!expense.isHasBill()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = fileStorageService.loadFileAsResource(expense.getBillPath());
        String contentType = expense.getBillFileType() != null ? expense.getBillFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + expense.getBillFileName() + "\"")
                .body(resource);
    }
}
