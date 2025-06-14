package com.oauth2.DetailPage.Dto;

import com.oauth2.Drug.Domain.DrugStorageCondition;

public record StorageDetail (
    DrugStorageCondition.Category category,
    String value,
    boolean active
){}

