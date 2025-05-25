package com.oauth2.Drug.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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
    private String manufacturer;
    private Date approvalDate;

    @Column(columnDefinition = "TEXT")
    private String packaging;

    private String form;
    private String atcCode;

    @Enumerated(EnumType.STRING)
    private Tag tag;

    public enum Tag {
        EXPERT, COMMON
    }

    // getter, setter 생략
} 