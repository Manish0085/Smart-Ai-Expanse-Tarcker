package com.example.ai_expanse_tacker.expense.service;

import com.example.ai_expanse_tacker.ai.dto.AiIntentResponse;
import com.example.ai_expanse_tacker.expense.entity.Expense;
import com.example.ai_expanse_tacker.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    private final ExpenseRepository repo;

    public ExpenseService(ExpenseRepository repo) {
        this.repo = repo;
    }

    public Expense saveFromAi(AiIntentResponse ai, UUID userId) {
        Expense e = new Expense();
        e.setUserId(userId);
        e.setAmount(ai.getAmount());
        e.setCategory(ai.getCategory());
        e.setNote(ai.getNote());
        e.setDate(LocalDate.now());
        return repo.save(e);
    }

    public List<Expense> getExpensesByUser(UUID userId) {
        return repo.findByUserId(userId);
    }

    public String deleteExpenseByKeyword(String keyword, UUID userId) {
        if (keyword == null || keyword.isEmpty())
            return "Please specify what you want to delete.";
        List<Expense> expenses = repo.findByUserId(userId);
        Expense toDelete = expenses.stream()
                .filter(e -> {
                    String note = (e.getNote() != null ? e.getNote() : "").toLowerCase();
                    String cat = (e.getCategory() != null ? e.getCategory() : "").toLowerCase();
                    String kw = keyword.toLowerCase();
                    return (note.contains(kw) || kw.contains(note)) && !note.isEmpty() ||
                            (cat.contains(kw) || kw.contains(cat)) && !cat.isEmpty();
                })
                .findFirst().orElse(null);

        if (toDelete != null) {
            repo.delete(toDelete);
            return "Successfully deleted expense: "
                    + (toDelete.getNote() != null ? toDelete.getNote() : toDelete.getCategory());
        }
        return "Could not find an expense matching '" + keyword + "' to delete.";
    }
}