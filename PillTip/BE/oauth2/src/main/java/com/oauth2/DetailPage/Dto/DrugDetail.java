package com.oauth2.DetailPage.Dto;

import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Domain.DrugEffect;
import com.oauth2.Drug.Domain.DrugStorageCondition;
import com.oauth2.Search.Dto.IngredientDetail;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Builder
public record DrugDetail (
    Long id,
    String name,
    String manufacturer,
    List<IngredientDetail> ingredients,
    String form,
    String packaging,
    String atcCode,
    Drug.Tag tag,
    Date approvalDate,
    List<DrugStorageCondition> storageDetails,
    List<DrugEffect> effectDetails
) {}
