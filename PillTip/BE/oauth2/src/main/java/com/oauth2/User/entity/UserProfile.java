// author : mireutale
// description : 유저 프로필 엔티티

package com.oauth2.User.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_profile")
public class UserProfile {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @JsonBackReference
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user; // 유저 프로필 user_id를 user 테이블의 id와 매핑

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal height;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(name = "health_status", columnDefinition = "TEXT")
    private String healthStatus;

    @Column(name = "disease_info", columnDefinition = "TEXT")
    private String diseaseInfo;

    @Column(name = "allergy_info", columnDefinition = "TEXT")
    private String allergyInfo;

    @Setter
    @Column(name = "pregnant", columnDefinition = "boolean default false")
    private boolean pregnant;

    @Builder
    public UserProfile(User user, Integer age, Gender gender, BigDecimal height, BigDecimal weight,
                      LocalDate birthDate, String phone, String healthStatus,
                      String diseaseInfo, String allergyInfo) {
        this.user = user;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.birthDate = birthDate;
        this.phone = phone;
        this.healthStatus = healthStatus;
        this.diseaseInfo = diseaseInfo;
        this.allergyInfo = allergyInfo;
    }
}

