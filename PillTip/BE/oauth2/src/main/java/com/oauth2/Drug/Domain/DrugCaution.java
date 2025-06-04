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

    @Column(columnDefinition = "TEXT")
    private String conditionValue; //조건

    @Column(columnDefinition = "TEXT")
    private String dosageForm; //제형

    @Column(columnDefinition = "TEXT")
    private String note; //비고

    @Column(columnDefinition = "TEXT")
    private String approvalInfo; //허가사항

    //PERIOD : 투여기간 주의
    //PREGNANCY : 임부금기
    //AGE: 연령금기
    //ELDER: 노인주의
    //LACTATION: 수유부주의
    public enum ConditionType {
        PERIOD, PREGNANCY, AGE,
        ELDER, LACTATION, OVERDOSE
    }

} 