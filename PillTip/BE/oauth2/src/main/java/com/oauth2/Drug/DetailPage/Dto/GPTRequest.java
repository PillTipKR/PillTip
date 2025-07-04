package com.oauth2.Drug.DetailPage.Dto;

import java.util.List;
import java.util.Map;

public record GPTRequest(
        String model,
        List<Map<String, String>> messages,
        double temperature,
        int max_tokens
){}

