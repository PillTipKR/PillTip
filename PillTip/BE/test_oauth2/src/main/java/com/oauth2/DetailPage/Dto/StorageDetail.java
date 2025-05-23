package com.oauth2.DetailPage.Dto;

import com.oauth2.Drug.Domain.DrugStorageCondition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageDetail {
    private DrugStorageCondition.Category category;
    private String description;
    private String iconUrl;
}
