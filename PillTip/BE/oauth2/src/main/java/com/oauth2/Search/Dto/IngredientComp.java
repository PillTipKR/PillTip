package com.oauth2.Search.Dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class IngredientComp implements Comparable<IngredientComp> {
    private String name;
    private Float dose;
    private String backup;
    private boolean isMain;

    @Override
    public int compareTo(IngredientComp o) {
        return this.dose.compareTo(o.dose);
    }
}

