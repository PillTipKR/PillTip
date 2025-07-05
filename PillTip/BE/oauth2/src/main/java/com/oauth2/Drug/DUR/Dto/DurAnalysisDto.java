package com.oauth2.Drug.DUR.Dto;

import java.util.List;

public record DurAnalysisDto(
        String title,
        List<DurDto> durDtos
) {}
