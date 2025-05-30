package com.oauth2.Drug.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "drug_interactions")
public class DrugInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interactionId;

    @Column(nullable = false)
    private Long ingredientId1;
    @Column(nullable = false)
    private Long ingredientId2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionLevel interactionLevel;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String description;

    public enum InteractionLevel {
        CAUTION, TABOO, INFO
    }
    // getter, setter 생략
} 