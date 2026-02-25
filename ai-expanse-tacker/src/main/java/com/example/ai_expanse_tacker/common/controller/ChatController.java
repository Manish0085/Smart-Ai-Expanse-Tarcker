package com.example.ai_expanse_tacker.common.controller;

import com.example.ai_expanse_tacker.chat.entity.ChatMessage;
import com.example.ai_expanse_tacker.chat.service.ChatService;
import com.example.ai_expanse_tacker.common.service.OrchestratorService;
import com.example.ai_expanse_tacker.common.utils.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final OrchestratorService orchestratorService;
    private final ChatService chatService;

    public ChatController(OrchestratorService orchestratorService, ChatService chatService) {
        this.orchestratorService = orchestratorService;
        this.chatService = chatService;
    }

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Save user message to history
        chatService.saveMessage(SecurityUtils.getCurrentUserId(), userMessage, "user");

        Object result = orchestratorService.processInput(userMessage);

        // Get the response text to save to history
        String botResponse = extractBotResponse(result);
        chatService.saveMessage(SecurityUtils.getCurrentUserId(), botResponse, "bot");

        Map<String, Object> response = new HashMap<>();
        response.put("message", userMessage);
        response.put("result", result);
        return response;
    }

    @GetMapping("/history")
    public List<ChatMessage> getHistory() {
        return chatService.getHistory(SecurityUtils.getCurrentUserId());
    }

    private String extractBotResponse(Object result) {
        if (result instanceof String)
            return (String) result;
        if (result instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) result;
            if (map.containsKey("message"))
                return (String) map.get("message");
            if (map.containsKey("intent") && "SHOW_REPORT".equals(map.get("intent"))) {
                return "Here is your financial report.";
            }
        }
        return "Processed your request.";
    }
}
