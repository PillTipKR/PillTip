package com.oauth2.DUR.Dto;

import com.oauth2.Search.Dto.IngredientDetail;
import com.oauth2.Search.Dto.SearchIndexDTO;

import java.util.List;

public record DurTagDto (
        Long id,
        String drugName,
        List<IngredientDetail> ingredients,
        String manufacturer,
        List<DurDetail> tag
)
{}
