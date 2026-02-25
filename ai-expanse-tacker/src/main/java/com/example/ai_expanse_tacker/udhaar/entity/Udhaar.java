package com.example.ai_expanse_tacker.udhaar.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
public class Udhaar {
    @Id
    @GeneratedValue
    private Long id;

    private UUID userId;
    private String person;
    private Double amount;
    private String note;
    private boolean given;
    private LocalDate date;
}