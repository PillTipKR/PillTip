package com.oauth2.DetailPage.Dto;

import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Domain.DrugEffect;
import com.oauth2.Drug.Domain.DrugStorageCondition;
import com.oauth2.Search.Dto.IngredientDetail;

import java.util.Date;
import java.util.List;

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
