package com.oauth2.Elasticsearch.Dto;

public record ElasticsearchDTO(
        String type,   // "drug", "ingredient", "manufacturer"
        String value   // 자동완성에 노출할 텍스트
) {}
