package com.oauth2.AutoComplete.Dto;

public record AutocompleteDTO(
        String type,   // "drugName", "ingredient", "manufacturer"
        String value   // 자동완성에 노출할 텍스트
) {}
