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

    private Long groupId1;
    private Long groupId2;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String note;
}
