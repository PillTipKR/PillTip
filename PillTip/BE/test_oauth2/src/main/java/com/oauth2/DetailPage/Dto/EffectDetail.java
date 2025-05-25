package com.oauth2.DetailPage.Dto;

import com.oauth2.Drug.Domain.DrugEffect;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EffectDetail {
    private DrugEffect.Type Type;

    private String effect;
}
