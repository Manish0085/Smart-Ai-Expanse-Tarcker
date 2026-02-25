package com.example.ai_expanse_tacker.chat.service;

import com.example.ai_expanse_tacker.chat.entity.ChatMessage;
import com.example.ai_expanse_tacker.chat.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final ChatMessageRepository repository;

    public ChatService(ChatMessageRepository repository) {
        this.repository = repository;
    }

    public void saveMessage(UUID userId, String content, String sender) {
        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setContent(content);
        message.setSender(sender);
        message.setTimestamp(LocalDateTime.now());
        repository.save(message);
    }

    public List<ChatMessage> getHistory(UUID userId) {
        return repository.findByUserIdOrderByTimestampAsc(userId);
    }
}
