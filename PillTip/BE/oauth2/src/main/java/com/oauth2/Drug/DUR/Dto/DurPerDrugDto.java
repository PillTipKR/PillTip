package com.oauth2.Drug.DUR.Dto;

import java.util.List;

//약마다의 DUR 묶기
public record DurPerDrugDto(
        String drugName,
        List<DurTagDto> durtags
) {}
