package com.oauth2.Drug.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "drug_therapeutic_dup")
public class DrugTherapeuticDup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String className;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String category;

    @Column(nullable = false)
    private Long ingredientId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String note;


}
