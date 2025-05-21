// PillTip\BE\src\main\java\com\example\oauth2\entity\User.java
// author : mireutale
// date : 2025-05-21
// description : Users(사용자) 엔티티

package com.oauth2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Entity //JPA에서 DB테이블과 매핑되는 클래스임을 명시
@Table(name = "users") //DB에서 테이블 이름
@Getter // 모든 필드의 Getter 메서드 생성
@Setter // 모든 필드의 Setter 메서드 생성
@NoArgsConstructor // 기본 생성자 생성
@Builder // 빌더 패턴 사용
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 생성
public class User {
    @Id // 기본키 지정
    @Builder.Default // 빌더 대상에 포함, User.builder().build() 시 자동으로 UUID.randomUUID()가 실행되어 들어감
    @Column(name = "uuid", columnDefinition = "BINARY(16)")
    private final UUID uuid = UUID.randomUUID();

    @Enumerated(EnumType.STRING) // 열거형 타입 지정
    @Column(name = "login_type", nullable = false) // 로그인 타입 지정, social or idpw
    private LoginType loginType;

    @Column(name = "social_id") // length = 255
    private String socialId;

    @Column(name = "password_hash") // length = 255
    private String passwordHash;

    @Column(unique = true, length = 50) // 닉네임 지정
    private String nickname;

    @Column(name = "profile_photo_url") // length = 255
    private String profilePhotoUrl;

    @Column(name = "terms", columnDefinition = "BOOLEAN DEFAULT FALSE") // 이용약관 동의 지정
    private boolean agreedTerms;

    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP") // 생성일 지정
    private LocalDateTime createdAt;

    // 유저 프로필 1대 1 관계
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile userProfile; 

    // 유저 관심사 1대 1 관계
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Interests interests;

    // 유저 동의사항 1대 1 관계
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserPermissions userPermissions;

    // 유저 위치 1대 1 관계
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserLocation userLocation;

    // 유저 문진표 1대 N 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PatientQuestionnaire> questionnaires;

    @PrePersist // 엔티티 저장 전 실행
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public User update(String nickname, String profilePhotoUrl) {
        this.nickname = nickname; // 닉네임 업데이트
        this.profilePhotoUrl = profilePhotoUrl; // 프로필 사진 업데이트
        return this; // 업데이트된 유저 반환
    }
} 