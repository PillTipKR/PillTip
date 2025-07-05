package com.oauth2.Drug.DUR.Dto;

import java.util.List;

public record DurPerDrugDto(
        String drugName,
        List<DurTagDto> durtags
) {}
