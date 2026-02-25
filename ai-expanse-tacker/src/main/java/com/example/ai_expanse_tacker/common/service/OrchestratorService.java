package com.example.ai_expanse_tacker.common.service;

import com.example.ai_expanse_tacker.ai.dto.AiIntentResponse;
import com.example.ai_expanse_tacker.ai.prompt.SystemPrompt;
import com.example.ai_expanse_tacker.ai.service.OpenRouterAiService;
import com.example.ai_expanse_tacker.common.utils.SecurityUtils;
import com.example.ai_expanse_tacker.expense.entity.Expense;
import com.example.ai_expanse_tacker.expense.service.ExpenseService;
import com.example.ai_expanse_tacker.udhaar.entity.Udhaar;
import com.example.ai_expanse_tacker.udhaar.service.UdhaarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrchestratorService {

    private final OpenRouterAiService aiService;
    private final ExpenseService expenseService;
    private final UdhaarService udhaarService;
    private final ObjectMapper objectMapper;

    public OrchestratorService(OpenRouterAiService aiService,
            ExpenseService expenseService,
            UdhaarService udhaarService,
            ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.expenseService = expenseService;
        this.udhaarService = udhaarService;
        this.objectMapper = objectMapper;
    }

    public Object processInput(String userInput) {
        UUID userId = SecurityUtils.getCurrentUserId();

        // Build RAG Context
        String ragContext = buildUserContext(userId);

        String prompt = SystemPrompt.BASE_PROMPT
                .replace("{RAG_CONTEXT}", ragContext)
                .replace("{USER_INPUT}", userInput);

        try {
            String aiResponseJson = aiService.chat(prompt);
            System.out.println("Raw AI Response: [" + aiResponseJson + "]");
            aiResponseJson = cleanJsonResponse(aiResponseJson);
            System.out.println("Cleaned AI JSON: [" + aiResponseJson + "]");

            AiIntentResponse aiIntent = objectMapper.readValue(aiResponseJson, AiIntentResponse.class);

            return switch (aiIntent.getIntent()) {
                case ADD_EXPENSE -> {
                    var e = expenseService.saveFromAi(aiIntent, userId);
                    yield Map.of(
                            "message", aiIntent.getNote() != null ? aiIntent.getNote() : "Expense saved.",
                            "expenses", List.of(e),
                            "format", "TABLE",
                            "intent", "ADD_EXPENSE",
                            "amount", aiIntent.getAmount() != null ? aiIntent.getAmount() : 0.0);
                }
                case UDHAAR_GIVEN -> {
                    var u = udhaarService.saveGiven(aiIntent, userId);
                    yield Map.of(
                            "message", aiIntent.getNote() != null ? aiIntent.getNote() : "Udhaar given saved.",
                            "udhaars", List.of(u),
                            "format", "TABLE",
                            "intent", "UDHAAR_GIVEN",
                            "amount", aiIntent.getAmount() != null ? aiIntent.getAmount() : 0.0);
                }
                case UDHAAR_TAKEN -> {
                    var u = udhaarService.saveTaken(aiIntent, userId);
                    yield Map.of(
                            "message", aiIntent.getNote() != null ? aiIntent.getNote() : "Udhaar taken saved.",
                            "udhaars", List.of(u),
                            "format", "TABLE",
                            "intent", "UDHAAR_TAKEN",
                            "amount", aiIntent.getAmount() != null ? aiIntent.getAmount() : 0.0);
                }
                case SHOW_REPORT -> generateReport(userId);
                case QUERY_HISTORY -> handleQueryHistory(aiIntent, userId);
                case GENERAL_CHAT ->
                    Map.of("message", aiIntent.getNote() != null ? aiIntent.getNote()
                            : "Hello! How can I help with your finances?", "format", "TEXT");
                case DELETE_ENTRY -> handleDeleteEntry(aiIntent, userId);
                case GET_SUGGESTIONS -> Map.of("message", aiIntent.getNote() != null ? aiIntent.getNote()
                        : "I'm analyzing your spending. Ask me for a specific category summary!", "format", "TEXT");
                default -> Map.of("message",
                        aiIntent.getNote() != null ? aiIntent.getNote() : "Neural link interrupted. Please try again.",
                        "format", "TEXT");
            };
        } catch (Exception e) {
            System.err.println("Orchestration Error: " + e.getMessage());
            return "Neural link interrupted (AI Service Error). Please try again later. Detail: " + e.getMessage();
        }
    }

    private String cleanJsonResponse(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

    private String buildUserContext(UUID userId) {
        List<Expense> expenses = expenseService.getExpensesByUser(userId);
        List<Udhaar> udhaars = udhaarService.getUdhaarsByUser(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("Recent Expenses: \n");
        expenses.stream().limit(10).forEach(e -> {
            double amt = e.getAmount() != null ? e.getAmount() : 0.0;
            String cat = e.getCategory() != null ? e.getCategory() : "Misc";
            String note = e.getNote() != null ? e.getNote() : "";
            sb.append(String.format("- %s: %.2f on %s (Note: %s)\n", e.getDate(), amt, cat, note));
        });

        sb.append("\nUdhaar History: \n");
        udhaars.stream().limit(10).forEach(u -> {
            double amt = u.getAmount() != null ? u.getAmount() : 0.0;
            String person = u.getPerson() != null ? u.getPerson() : "Someone";
            String note = u.getNote() != null ? u.getNote() : "";
            sb.append(String.format("- %s: %.2f %s %s (%s)\n", u.getDate(),
                    amt, u.isGiven() ? "lent to" : "borrowed from", person, note));
        });

        return sb.toString();
    }

    private Map<String, Object> generateReport(UUID userId) {
        List<Expense> expenses = expenseService.getExpensesByUser(userId);
        List<Udhaar> udhaars = udhaarService.getUdhaarsByUser(userId);

        Map<String, Object> report = new HashMap<>();
        report.put("expenses", expenses);
        report.put("udhaars", udhaars);
        report.put("totalExpense",
                expenses.stream().mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0).sum());
        report.put("totalUdhaarGiven", udhaars.stream().filter(u -> u.isGiven())
                .mapToDouble(u -> u.getAmount() != null ? u.getAmount() : 0.0).sum());
        report.put("totalUdhaarTaken", udhaars.stream().filter(u -> !u.isGiven())
                .mapToDouble(u -> u.getAmount() != null ? u.getAmount() : 0.0).sum());
        report.put("intent", "SHOW_REPORT");
        report.put("format", "TABLE"); // Default to table for full reports
        return report;
    }

    private Object handleQueryHistory(AiIntentResponse ai, UUID userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ai.getNote());
        response.put("intent", "QUERY_HISTORY");
        response.put("format", ai.getFormat()); // Pass format from AI (TEXT, TABLE, CHART)

        // Include relevant data based on intent detection
        if ("TABLE".equals(ai.getFormat()) || "CHART".equals(ai.getFormat())) {
            response.put("expenses", expenseService.getExpensesByUser(userId));
            response.put("udhaars", udhaarService.getUdhaarsByUser(userId));
        }

        return response;
    }

    private Object handleDeleteEntry(AiIntentResponse ai, UUID userId) {
        String keyword = ai.getNote();
        String resultMsg;
        String expenseResult = expenseService.deleteExpenseByKeyword(keyword, userId);

        if (expenseResult.startsWith("Successfully")) {
            resultMsg = expenseResult;
        } else {
            resultMsg = udhaarService.deleteUdhaarByKeyword(keyword, userId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", resultMsg);
        response.put("intent", "DELETE_SUCCESS");
        return response;
    }
}
