package com.example.ai_expanse_tacker.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class ChatMessage {
    @Id
    @GeneratedValue
    private Long id;

    private UUID userId;
    private String content;
    private String sender; // "user" or "bot"
    private LocalDateTime timestamp;
}
