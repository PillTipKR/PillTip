package com.oauth2.Search.Dto;

import java.util.List;

public record SearchIndexDTO (
        Long id,
        String drugName,
        List<IngredientDetail> ingredients,
        String manufacturer
) {}
