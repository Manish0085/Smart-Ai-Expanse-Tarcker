package com.example.ai_expanse_tacker.ai.service;

import com.example.ai_expanse_tacker.ai.dto.AiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenRouterAiService {

        private final WebClient webClient;
        private final String model;

        public OpenRouterAiService(
                        WebClient.Builder builder,
                        @Value("${openrouter.api.key}") String apiKey,
                        @Value("${openrouter.api.base-url}") String baseUrl,
                        @Value("${openrouter.api.model}") String model) {

                this.model = model;

                this.webClient = builder
                                .baseUrl(baseUrl)
                                .defaultHeader("Authorization", "Bearer " + apiKey)
                                .defaultHeader("Content-Type", "application/json")
                                .build();
        }

        public String chat(String prompt) {

                Map<String, Object> body = Map.of(
                                "model", model,
                                "messages", List.of(
                                                Map.of("role", "user", "content", prompt)));
                System.out.println("Calling OpenRouter with model: " + model);

                AiResponse response = webClient.post()
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(AiResponse.class)
                                .block();

                if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                        throw new RuntimeException("AI service returned an empty response.");
                }

                return response
                                .getChoices()
                                .get(0)
                                .getMessage()
                                .getContent();
        }
}