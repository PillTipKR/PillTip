package com.oauth2.Drug.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "drug_effects")
public class DrugEffect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long drugId;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    public enum Type {
        EFFECT, USAGE, CAUTION
    }
    // getter, setter 생략
} 