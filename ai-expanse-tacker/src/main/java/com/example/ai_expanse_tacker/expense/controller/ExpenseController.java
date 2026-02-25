package com.example.ai_expanse_tacker.expense.controller;

import com.example.ai_expanse_tacker.common.utils.SecurityUtils;
import com.example.ai_expanse_tacker.expense.entity.Expense;
import com.example.ai_expanse_tacker.expense.service.ExpenseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public List<Expense> getAllExpenses() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return expenseService.getExpensesByUser(userId);
    }
}
