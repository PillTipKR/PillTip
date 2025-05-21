// PillTip\BE\src\main\java\com\example\oauth2\entity\UserProfile.java
// author : mireutale
// date : 2025-05-19
// description : user_profile(사용자 프로필) 엔티티

package com.oauth2.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Builder // 빌더 패턴 사용
@Table(name = "user_profile")
public class UserProfile {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID uuid;

    @OneToOne
    @JoinColumn(name = "uuid", referencedColumnName = "uuid")
    private User user;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private BigDecimal height;
    private BigDecimal weight;

    private LocalDate birthDate;

    private String phone;
    private String healthStatus;
    private String takingPills;
    private String diseaseInfo;
    private String allergyInfo;
}
