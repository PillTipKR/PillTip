package com.oauth2.Search.Dto;


import java.util.List;

public record DrugDTO(
        Long id,
        String drugName,
        List<IngredientDetail> ingredients,
        String manufacturer
) {}
