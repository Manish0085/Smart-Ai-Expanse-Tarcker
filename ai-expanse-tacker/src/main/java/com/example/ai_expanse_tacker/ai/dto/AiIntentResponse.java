package com.example.ai_expanse_tacker.ai.dto;

import com.example.ai_expanse_tacker.common.enums.IntentType;
import lombok.Data;

@Data
public class AiIntentResponse {

    private IntentType intent;
    private Double amount;
    private String category;
    private String person;
    private String note;
    private String format; // TEXT, TABLE, CHART
}