package com.oauth2.DetailPage.Dto;

import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Domain.DrugEffect;
import com.oauth2.Drug.Domain.DrugStorageCondition;
import com.oauth2.Search.Dto.IngredientDetail;
import lombok.Builder;

import java.util.Date;
import java.util.List;
import java.util.Set;

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
    List<StorageDetail> storageDetails,
    List<EffectDetail> effectDetails
) {}
