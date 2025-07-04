package com.oauth2.Drug.DetailPage.Dto;

import com.oauth2.Drug.DrugImport.Domain.DrugEffect;

public record EffectDetail (
        DrugEffect.Type Type,
        String effect
){}

