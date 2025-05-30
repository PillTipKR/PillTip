package com.oauth2.Drug.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "drug_cautions")
public class DrugCaution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cautionId;

    @Column(nullable = false)
    private Long ingredientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionType conditionType;

    @Column(nullable = false)
    private String conditionValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;

    @Column(columnDefinition = "TEXT")
    private String description;

    public enum ConditionType {
        GENDER, PREGNANCY, AGE, CONDITION
    }
    public enum Level {
        CAUTION, TABOO
    }
    // getter, setter 생략
} 