package com.oauth2.Drug.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "drugs")
public class Drug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String code;

    @Column(nullable = false)
    private String manufacturer;
    private Date approvalDate;

    @Column(columnDefinition = "TEXT",nullable=false)
    private String packaging;

    private String form;
    private String atcCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tag tag;

    public enum Tag {
        EXPERT, COMMON
    }

    private String image;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "drug", cascade = CascadeType.ALL)
    private Set<DrugEffect> drugEffects;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "drug", cascade = CascadeType.ALL)
    private Set<DrugStorageCondition> storageConditions;


    // getter, setter 생략
}
