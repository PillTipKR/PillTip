package com.oauth2.Drug.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "drug_storage_conditions")
public class DrugStorageCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long drugId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String value;
    private String iconUrl;

    public enum Category {
        TEMPERATURE, CONTAINER, HUMID, LIGHT, PLACE
    }
    // getter, setter 생략
} 