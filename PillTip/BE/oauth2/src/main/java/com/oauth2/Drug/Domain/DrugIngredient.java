package com.oauth2.Drug.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "drug_ingredients")
public class DrugIngredient {
    @EmbeddedId
    private DrugIngredientId id;

    private Float amount;
    private String amountBackup;
    private String unit;

    // getter, setter 생략

    @Getter
    @Setter
    @Embeddable
    public static class DrugIngredientId implements java.io.Serializable {
        @Column(nullable = false)
        private Long drugId;
        @Column(nullable = false)
        private Long ingredientId;
        // equals, hashCode 생략
    }
} 