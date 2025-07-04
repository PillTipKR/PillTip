package com.oauth2.DUR.Dto;

import com.oauth2.Search.Dto.IngredientDetail;

import java.util.List;

public record SearchDurDto(
        Long id,
        String drugName,
        List<IngredientDetail> ingredients,
        String manufacturer,
        String imageUrl,
        List<DurTagDto> durTags
)
{}
