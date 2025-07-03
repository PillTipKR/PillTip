// author : mireutale
// description : 유저 엔티티
package com.oauth2.User.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.oauth2.Review.Domain.Review;
import com.oauth2.Review.Domain.ReviewLike;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity //JPA에서 DB테이블과 매핑되는 클래스임을 명시
@Table(name = "users") //DB에서 테이블 이름
@Getter // 모든 필드의 Getter 메서드 생성
@Setter
@NoArgsConstructor // 기본 생성자 생성
@Builder // 빌더 패턴 사용
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 생성
public class User {
    @Id // 기본키 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Id에 대해서 AUTO_INCREMENT 수행
    private Long id;

    @Enumerated(EnumType.STRING) // 열거형 타입 지정
    @Column(name = "login_type", nullable = false) // 로그인 타입 지정, social or idpw
    private LoginType loginType;

    @Column(name = "login_id", unique = true) // 로그인 ID
    private String loginId;

    @Column(name = "social_id", unique = true) // 소셜 로그인 Oauth2의 토큰
    private String socialId;

    @Column(name = "password_hash", unique = true) // 로그인 비밀번호 hash
    private String passwordHash;

    @Column(name = "user_email", unique = true) // 유저의 이메일
    private String userEmail;

    @Column(name = "profile_photo") // 유저의 프로필 사진 URL
    private String profilePhoto;

    @Column(nullable = false, unique = true) // 유저의 닉네임
    private String nickname;

    @Column(nullable = false) // 유저의 동의사항
    private boolean terms;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // onCreate에서 현재 시간을 가져오고, 이 값을 저장

    // 유저 프로필 1대 1 관계
    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile userProfile; 

    // 유저 관심사 1대 1 관계
    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Interests interests;

    // 유저 동의사항 1대 1 관계
    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserPermissions userPermissions;

    // 유저 위치 1대 1 관계
    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserLocation userLocation;

    // 유저 토큰 1대 1 관계
    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserToken userToken;

    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private FCMToken FCMToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLike> likes = new ArrayList<>();

    // 유저 문진표 1대 N 관계
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PatientQuestionnaire> questionnaires;

    // 복용 중인 약 1대 N 관계
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TakingPill> takingPills;

    @PrePersist // 엔티티 저장 전 실행
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public User update(String new_nickname, String new_profilePhoto) {
        this.nickname = new_nickname; // 닉네임 업데이트
        this.profilePhoto = new_profilePhoto; // 프로필 사진 업데이트
        return this; // 업데이트된 유저 반환
    }

    @Builder
    public User(LoginType loginType, String loginId, String socialId, String passwordHash,
                String profilePhoto, String nickname, boolean terms) {
        this.loginType = loginType;
        this.loginId = loginId;
        this.socialId = socialId;
        this.passwordHash = passwordHash;
        this.profilePhoto = profilePhoto;
        this.nickname = nickname;
        this.terms = terms;
    }
}
