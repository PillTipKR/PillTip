package com.oauth2.Review.Dto;

import com.oauth2.Review.Domain.TagType;

public record TagStatsDto(
        String mostUsedTagName,
        Long mostUsedTagCount,
        Long totalTagCount
){}
