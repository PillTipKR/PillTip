package com.oauth2.Review.Dto;


public record UserReviewResponse (
        Long drugId,
        String drugName,
        ReviewResponse reviews
)
{}
