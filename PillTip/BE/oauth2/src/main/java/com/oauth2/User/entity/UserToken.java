// author : mireutale
// description : jwt 토큰 엔티티

package com.oauth2.User.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserToken {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime accessTokenExpiry;

    @Column(nullable = false)
    private LocalDateTime refreshTokenExpiry;

    @Version
    private Long version;

    @Builder
    public UserToken(Long userId, User user, String accessToken, String refreshToken, LocalDateTime accessTokenExpiry, LocalDateTime refreshTokenExpiry) {
        this.userId = userId;
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    // 액세스 토큰과 리프레시 토큰 갱신
    public void updateTokens(String accessToken, String refreshToken, LocalDateTime accessTokenExpiry, LocalDateTime refreshTokenExpiry) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    // 액세스 토큰 갱신
    public void updateAccessToken(String accessToken, LocalDateTime accessTokenExpiry) {
        this.accessToken = accessToken;
        this.accessTokenExpiry = accessTokenExpiry;
    }
}

