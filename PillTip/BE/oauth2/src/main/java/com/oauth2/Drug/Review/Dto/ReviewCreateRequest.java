package com.oauth2.Drug.Review.Dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ReviewCreateRequest {
    private Long drugId;
    private Float rating;
    private String content;
    private List<String> imageUrls; // Firebase에서 업로드 후 받은 URL

    private Map<String, List<String>> tags; // "efficacy": [...], "sideEffect": [...], "other": [...]
}

