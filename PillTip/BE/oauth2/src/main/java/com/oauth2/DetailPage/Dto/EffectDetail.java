package com.oauth2.DetailPage.Dto;

import com.oauth2.Drug.Domain.DrugEffect;
import lombok.Getter;
import lombok.Setter;

public record EffectDetail (
        DrugEffect.Type Type,
        String effect
){}

