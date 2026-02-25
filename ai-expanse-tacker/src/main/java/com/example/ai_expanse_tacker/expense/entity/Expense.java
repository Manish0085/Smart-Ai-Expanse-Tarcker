package com.example.ai_expanse_tacker.expense.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
public class Expense {

    @Id
    @GeneratedValue
    private Long id;

    private UUID userId;
    private Double amount;
    private String category;
    private String note;
    private LocalDate date;
}