package com.oauth2.DetailPage.Dto;

import com.oauth2.Drug.Domain.DrugStorageCondition;
import lombok.Getter;
import lombok.Setter;

public record StorageDetail (
    DrugStorageCondition.Category category,
    String value,
    boolean active
){}

