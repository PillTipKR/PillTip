package com.oauth2.Review.Dto;

import com.oauth2.Review.Domain.TagType;

import java.util.Map;

public record ReviewStats(
    Long total,
    RatingStatsResponse ratingStatsResponse,
    Map<TagType, TagStatsDto> tagStatsByType
){}


