package com.oauth2.Search.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class IngredientDetail implements Comparable<IngredientDetail> {
    private String name;
    private String dose;
    private boolean isMain;

    @Override
    public int compareTo(IngredientDetail o) {
        return this.dose.compareTo(o.dose);
    }
}
