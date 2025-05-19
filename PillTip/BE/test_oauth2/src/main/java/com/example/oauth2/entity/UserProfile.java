// PillTip\BE\src\main\java\com\example\oauth2\entity\UserProfile.java
// author : mireutale
// date : 2025-05-19
// description : user_profile(사용자 프로필) 엔티티

package com.example.oauth2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserProfile {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID uuid;

    @OneToOne
    @MapsId
    @JoinColumn(name = "uuid")
    private User user;

    @Column
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(precision = 5, scale = 2)
    private BigDecimal height;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "birth_date", columnDefinition = "DATE")
    private LocalDate birthDate;

    @Column(length = 20)
    private String phone;
    
    @Column(name = "health_status", columnDefinition = "TEXT")
    private String healthStatus;

    @Column(name = "taking_pills", columnDefinition = "TEXT")
    private String takingPills;

    @Column(name = "disease_info", columnDefinition = "TEXT")
    private String diseaseInfo;

    @Column(name = "allergy_info", columnDefinition = "TEXT")
    private String allergyInfo;
} 