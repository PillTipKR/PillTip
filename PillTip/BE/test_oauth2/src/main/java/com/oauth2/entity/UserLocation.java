// PillTip\BE\src\main\java\com\example\oauth2\entity\UserLocation.java
// author : mireutale
// date : 2025-05-21
// description : Users_location(사용자 프로필) 엔티티
package com.oauth2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "user_location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLocation {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID uuid; // 유저 고유 식별 번호

    @OneToOne
    @MapsId
    @JoinColumn(name = "uuid")
    private User user; // 유저 테이블의 uuid 참조, 유저 1대 1 관계

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude; // 유저 위도

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude; // 유저 경도
} 