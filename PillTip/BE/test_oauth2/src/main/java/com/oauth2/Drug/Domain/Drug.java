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

    private String image;

    public enum Tag {
        EXPERT, COMMON
    }

    // getter, setter 생략
} 